package com.example.demo.service;

import com.example.demo.dto.OffMember;
import com.example.demo.dto.OfflineOperationDto;
import com.example.demo.dto.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OfflineOperationsMigr {

    /**
     * Сколько операций вставляем одним multi-row INSERT.
     * 54 параметра * 200 строк = 10 800 параметров (лимит драйвера PostgreSQL ~32 767).
     */
    private static final int OP_CHUNK = 200;

    private static final String[] OP_COLUMNS = {
            "ISSUEDBID", "BANKOPERATIONID", "ORDERNUMBER", "BRANCH",
            "CURRENCYCODE", "OPERATIONDATETIME", "BASEAMOUNT",
            "CURRENCYAMOUNT", "EKNPCODE", "DOCNUMBER", "DOCDATE",
            "DOCCATEGORY", "OPERATIONSTATUS", "OPERATIONREASON",
            "P_CHECKED", "P_TOEXTRACTBOOL", "DATE_INSERT", "DATE_UPDATE",
            "CHANGEDATE", "DOCTYPE", "DOCSUSPIC", "USER_NAME", "FINDATE",
            "PROPERTYNUMBER", "PROPERTY", "TRADENUMBER", "TRADEDATA",
            "SDP", "COUNTDAYS", "TYPETRANSACTION", "TRANS_TYPE", "SIC",
            "SOURCE_CHANNEL", "RISK_CHECKED", "PRODUCT", "SERVICE",
            "BSUSERNAME", "CURRENCYAMOUNT_CONTRACT",
            "BASEAMOUNT_CONTRACT", "CURRENCYAMOUNT_CB",
            "BASEAMOUNT_CB", "CB_COUNT", "TRANSACTION_PLACE",
            "TRANSACTION_COUNTRY", "TRANSACTION_AREA",
            "TRANSACTION_REGION", "TRANSACTION_CITY",
            "TRANSACTION_STREET", "TRANSACTION_HOUSE",
            "TRANSACTION_OFFICE", "TRANSACTION_POSTCODE",
            "ISONLINE", "ISIN", "CFI"
    };

    private static final String MEMBER_INSERT_SQL = """
            INSERT INTO TB_OFFLINEMEMBERS (
            operation_id, bankoperationid, name, clientid, bsclientid, bankclient, regopendate, countrycode, clienttype, clientrole,
            clientkind, account, bsaccount, bank, bankcountrycode, bankname, bankcity, ipdl, username, lastname,
            firstname, middlename, sdp, orgform, offshorezone, kbe, bsclientrole, accountopendate, agreementclientkind, rescountry,
            npdl, affilipdl, affilnpdl, p_citizenship, p_subaccount, is_resident, is_pdl, p_offshore_code, p_additional_info, docnumber,
            fpbankoffshore, tpoffshore, operterr, tpbankoffshore, fpoffshore, member_type_form, operationdatetime)
            VALUES (
                ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                ?, ?, ?, ?, ?, ?, ?
            )
            """;

    private final JdbcTemplate jdbcTemplate;

    public OfflineOperationsMigr(
            @Qualifier("postgresJdbcTemplate")
            JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public Response LoadOperations(List<OfflineOperationDto> dto) {
        Response response = new Response();
        response.setAction("0");

        if (dto == null || dto.isEmpty()) {
            log.info("LoadOperations: получен пустой список операций");
            response.setErrorCode(0);
            response.setErrorMessage(null);
            return response;
        }

        try {
            for (int from = 0; from < dto.size(); from += OP_CHUNK) {
                List<OfflineOperationDto> chunk =
                        dto.subList(from, Math.min(from + OP_CHUNK, dto.size()));
                processChunk(chunk);
                log.info("LoadOperations: чанк {}", chunk.size());
            }

            response.setAction("Operations and members processed");
            response.setErrorCode(200);
            response.setErrorMessage("SUCCESS");

        } catch (Exception e) {
            log.error("Failed to load operations and members", e);
            response.setErrorCode(400);
            response.setErrorMessage(e.getMessage());
            response.setAction("Error load operations");
            // чтобы @Transactional откатил изменения при ошибке:
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }

        return response;
    }

    private void processChunk(List<OfflineOperationDto> chunk) {
        // 1. Один multi-row INSERT на весь chunk.
        //    RETURNING отдаёт только реально вставленные строки (конфликтные пропускаются).
        List<Object> opArgs = new ArrayList<>(chunk.size() * OP_COLUMNS.length);
        for (OfflineOperationDto op : chunk) {
            Collections.addAll(opArgs, operationArgs(op));
        }

        Map<String, Long> inserted = new HashMap<>();
        jdbcTemplate.query(
                buildOperationInsertSql(chunk.size()),
                (RowCallbackHandler) rs -> inserted.put(
                        key(rs.getObject(2), rs.getObject(3)),
                        rs.getLong(1)),
                opArgs.toArray());

        // 2. Участники только для новых операций.
        //    remove() гарантирует, что при дубле операции внутри одной пачки
        //    участники будут вставлены один раз.
        List<Object[]> memberRows = new ArrayList<>();
        for (OfflineOperationDto op : chunk) {
            Long operationId = inserted.remove(key(op.getPIssuedbid(), op.getPBankoperationid()));
            if (operationId == null) {
                log.debug("Операция {} / {} уже существует, участники пропущены",
                        op.getPIssuedbid(), op.getPBankoperationid());
                continue;
            }
            if (op.getMembers() != null) {
                for (OffMember m : op.getMembers()) {
                    memberRows.add(memberArgs(operationId, op, m));
                }
            }
        }

        // Если что-то осталось в map, значит ключ из RETURNING не совпал с ключом из DTO
        // (например, разные типы/форматирование). Лучше откатить, чем молча потерять участников.
        if (!inserted.isEmpty()) {
            throw new IllegalStateException(
                    "Не удалось сопоставить вставленные операции с входными данными: " + inserted.keySet());
        }

        // 3. Участники одним JDBC-batch'ем
        if (!memberRows.isEmpty()) {
            jdbcTemplate.batchUpdate(MEMBER_INSERT_SQL, memberRows);
        }
    }

    private static String buildOperationInsertSql(int rows) {
        String oneRow = "(" + String.join(", ",
                Collections.nCopies(OP_COLUMNS.length, "?")) + ")";

        String values = String.join(", ",
                Collections.nCopies(rows, oneRow));
        return "INSERT INTO TB_OFFLINEOPERATIONS (" + String.join(", ", OP_COLUMNS) + ") "
                + "SELECT " + selectColumnsWithCasts() + " "
                + "FROM (VALUES " + values + ") AS v ("
                + String.join(", ", OP_COLUMNS) + ") "
                + "WHERE NOT EXISTS ("
                + "    SELECT 1 FROM TB_OFFLINEOPERATIONS t "
                + "    WHERE t.ISSUEDBID = v.ISSUEDBID "
                + "    AND t.BANKOPERATIONID = v.BANKOPERATIONID"
                + ") "
                + "ON CONFLICT (BANKOPERATIONID, ISSUEDBID) DO NOTHING "
                + "RETURNING ID, ISSUEDBID, BANKOPERATIONID";
    }
    private static String selectColumnsWithCasts() {
        List<String> columns = new ArrayList<>();

        for (String column : OP_COLUMNS) {
            switch (column) {
                // INTEGER (int4)
                case "ISSUEDBID",
                     "DOCCATEGORY",
                     "OPERATIONSTATUS",
                     "P_CHECKED",
                     "P_TOEXTRACTBOOL",
                     "DOCSUSPIC",
                     "COUNTDAYS",
                     "TYPETRANSACTION",
                     "RISK_CHECKED",
                     "CURRENCYAMOUNT_CB",
                     "BASEAMOUNT_CB",
                     "CB_COUNT",
                     "ISONLINE",
                     "TRANSACTION_PLACE" ->
                        columns.add(
                                "CAST(NULLIF(BTRIM(CAST(v." + column + " AS TEXT)), '') AS INTEGER) AS " + column
                        );

                // BIGINT (int8)
                case "ID_REC" ->
                        columns.add(
                                "CAST(NULLIF(BTRIM(CAST(v." + column + " AS TEXT)), '') AS BIGINT) AS " + column
                        );

                // NUMERIC
                case "BASEAMOUNT",
                     "CURRENCYAMOUNT",
                     "CURRENCYAMOUNT_CONTRACT",
                     "BASEAMOUNT_CONTRACT" ->
                        columns.add(
                                "CASE " +
                                        "WHEN NULLIF(BTRIM(CAST(v." + column + " AS TEXT)), '') IS NULL THEN NULL " +
                                        "WHEN BTRIM(CAST(v." + column + " AS TEXT)) ~ '^[+-]?[0-9]+([.][0-9]+)?$' " +
                                        "THEN CAST(BTRIM(CAST(v." + column + " AS TEXT)) AS NUMERIC) " +
                                        "ELSE NULL END AS " + column
                        );

                // DATE
                case "FINDATE",
                     "TRADEDATA" ->
                        columns.add(
                                "CAST(NULLIF(BTRIM(CAST(v." + column + " AS TEXT)), '') AS DATE) AS " + column
                        );

                // TIMESTAMP
                case "OPERATIONDATETIME",
                     "DOCDATE",
                     "CONTRACTDATE",
                     "DATE_INSERT",
                     "DATE_UPDATE",
                     "CHANGEDATE" ->
                        columns.add(
                                "CAST(NULLIF(BTRIM(CAST(v." + column + " AS TEXT)), '') AS TIMESTAMP) AS " + column
                        );

                // VARCHAR / TEXT
                case "BANKOPERATIONID",
                     "ORDERNUMBER",
                     "BRANCH",
                     "CURRENCYCODE",
                     "EKNPCODE",
                     "DOCNUMBER",
                     "OPERATIONREASON",
                     "DOCTYPE",
                     "PROPERTY",
                     "PROPERTYNUMBER",
                     "TRADENUMBER",
                     "TRANS_TYPE",
                     "SIC",
                     "SOURCE_CHANNEL",
                     "PRODUCT",
                     "SERVICE",
                     "BSUSERNAME",
                     "USER_NAME",
                     "SDP",
                     "TRANSACTION_COUNTRY",
                     "TRANSACTION_AREA",
                     "TRANSACTION_REGION",
                     "TRANSACTION_CITY",
                     "TRANSACTION_STREET",
                     "TRANSACTION_HOUSE",
                     "TRANSACTION_OFFICE",
                     "TRANSACTION_POSTCODE",
                     "ISIN",
                     "CFI" ->
                        columns.add(
                                "CAST(v." + column + " AS TEXT) AS " + column
                        );

                // Остальные колонки оставляем без преобразования
                default ->
                        columns.add("v." + column);
            }
        }

        return String.join(", ", columns);
    }

    private static String key(Object issuedBid, Object bankOperationId) {
        return issuedBid + "|" + bankOperationId;
    }

    private static Object[] operationArgs(OfflineOperationDto op) {
        return new Object[]{
                op.getPIssuedbid(),
                op.getPBankoperationid(),
                op.getPOrdernumber(),
                op.getPBranch(),
                op.getPCurrencycode(),
                op.getPOperationdatetime(),
                op.getPBaseamount(),
                op.getPCurrencyamount(),
                op.getPEknpcode(),
                op.getPDocnumber(),
                op.getPDocdate(),
                op.getPDoccategory(),
                op.getPOperationstatus(),
                op.getPOperationreason(),
                op.getPChecked(),
                op.getPToextractbool(),
                op.getPDateInsert(),
                op.getPDateUpdate(),
                op.getChangedate(),
                op.getPDoctype(),
                op.getPDocsuspic(),
                op.getPUsername(),
                op.getPFinDate(),
                op.getPPropertynumber(),
                op.getPProperty(),
                op.getPTradenumber(),
                op.getPTradedate(),
                op.getPSdp(),
                op.getPCountday(),
                op.getPTypetransaction(),
                op.getPTranstype(),
                op.getPSic(),
                op.getPSourcechannel(),
                op.getRiskChecked(),
                op.getPProduct(),
                op.getPService(),
                op.getBsUsername(),
                op.getPCurrencyamountContract(),
                op.getPBaseamountContract(),
                op.getPCurrencyamountCb(),
                op.getPBaseamountCb(),
                op.getPCbCount(),
                op.getPTransactionPlace(),
                op.getPTransactionCountry(),
                op.getPTransactionArea(),
                op.getPTransactionRegion(),
                op.getPTransactionCity(),
                op.getPTransactionStreet(),
                op.getPTransactionHouse(),
                op.getPTransactionOffice(),
                op.getPTransactionPostcode(),
                op.getPIsonline(),
                op.getPIsin(),
                op.getPCfi()
        };
    }

    private static Object[] memberArgs(Long operationId, OfflineOperationDto op, OffMember m) {
        return new Object[]{
                //operation_id, bankoperationid, name, clientid, bsclientid, bankclient, regopendate, countrycode, clienttype, clientrole,
                operationId,
                op.getPBankoperationid(),
                m.getPName(),
                m.getPClientid(),
                m.getPBsclientid(),
                m.getPBankClient(),
                m.getPRegopendate(),
                m.getPCountrycode(),
                m.getPClientType(),
                m.getPClientrole(),
                //clientkind, account, bsaccount, bank, bankcountrycode, bankname, bankcity, ipdl, username, lastname,
                m.getPClientkind(),
                m.getPAccount(),
                m.getPBsaccount(),
                m.getPBank(),
                m.getPBankcountrycode(),
                m.getPBankname(),
                m.getPBankcity(),
                m.getPIpdl(),
                m.getPUsername(),
                m.getPLastname(),
                //firstname, middlename, sdp, orgform, offshorezone, kbe, bsclientrole, accountopendate, agreementclientkind, rescountry,
                m.getPFirstname(),
                m.getPMiddlename(),
                m.getPSdp(),
                m.getPOrgform(),
                m.getOffshoreZone(),
                m.getKbe(),
                null, //bsclientrole
                m.getPAccountopendate(),
                null, //agreementclientkind
                m.getPCitizenship(),
                //npdl, affilipdl, affilnpdl, p_citizenship, p_subaccount, is_resident, is_pdl, p_offshore_code, p_additional_info, docnumber,
                0, 0, 0, //npdl, affilipdl, affilnpdl
                m.getPCitizenship(),
                m.getPSubaccount(),
                null, //is_resident
                m.getPIpdl(),
                null, //p_offshore_code
                null, //p_additional_info
                m.getPDocNumber(),
                //fpbankoffshore, tpoffshore, operterr, tpbankoffshore, fpoffshore, member_type_form, operationdatetime
                m.getPFpBankOffshore(),
                m.getPTpOffshore(),
                null, //operterr
                m.getPTpBankOffshore(),
                m.getPFpOffshore(),
                m.getPMembertypeform(),
                m.getPOperationdate()
        };
    }
}


