package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.mapper.RelatedsMapper;
import com.example.demo.mapper.SuspiciousMemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


@Slf4j
@Service
//@RequiredArgsConstructor
public class ClientCardService {

    @Value("${clientCardServiceUL}")
    private String clientCardServiceUL;

    @Value("${clientCardServiceFL}")
    private String clientCardServiceFL;

    @Value("${clientCardServiceIP}")
    private String clientCardServiceIP;

    @Value("${clientCardServiceRole}")
    private String clientCardServiceRole;

    @Value("${spring.datasource.oracle.schema:${DB_SCHEMA_USER:AML_USER_ALTYN}}")
    private String oracleSchemaUser;

    private final JdbcTemplate jdbcTemplate;
    private final RestClient restClient;
    private final SuspiciousMemberMapper suspiciousMemberMapper;
    private final RelatedsMapper relatedsMapper;

    public ClientCardService(
            @Qualifier("oracleJdbcTemplate") JdbcTemplate jdbcTemplate,
            RestClient restClient,
            SuspiciousMemberMapper suspiciousMemberMapper,
            RelatedsMapper relatedsMapper) {

        this.jdbcTemplate = jdbcTemplate;
        this.restClient = restClient;
        this.suspiciousMemberMapper = suspiciousMemberMapper;
        this.relatedsMapper = relatedsMapper;
    }

    public Response sendClientUL(SetClientUL client) {
        return send(clientCardServiceUL, client);
    }

    public Response sendClientFL(SetClientFL client) {return send(clientCardServiceFL, client);}

    public Response sendClientIP(SetClientIP client) {
        return send(clientCardServiceIP, client);
    }

    public Response sendClientRole(ClientLinkRequest client) {
        return send(clientCardServiceRole, client);
    }

    private Response send(String url, Object client) {
        try {
            return restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(client)
                    .retrieve()
                    .body(Response.class);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Ошибка HTTP при отправке клиента. URL={}, status={}, response={}, body={}",
                    url, e.getStatusCode(), e.getResponseBodyAsString(), client, e);
            return new Response();

        } catch (ResourceAccessException e) {
            // Ссылка недоступна: UnknownHostException, ConnectException, SocketTimeoutException и т.п.
            Throwable cause = e.getMostSpecificCause();
            log.error("Ссылка недоступна. URL={}, причина={}: {}, body={}",
                    url, cause.getClass().getSimpleName(), cause.getMessage(), client, e);
            return new Response();

        } catch (Exception e) {
            log.error("Ошибка при отправке клиента. URL={}, body={}", url, client, e);
            return new Response();
        }
    }

    public Response send(Suspiciousmembers suspMember) {
        Response res = null;
        switch (suspMember.getPMembertype()) {
            case 1:
                SetClientUL ClientUL = suspiciousMemberMapper.toUlRequest(suspMember);
                res = sendClientUL(ClientUL);
                break;
            case 2:
                SetClientFL ClientFL = suspiciousMemberMapper.toFlRequest(suspMember);
                res = sendClientFL(ClientFL);
                break;
            case 3:
                SetClientIP ClientIP = suspiciousMemberMapper.toIPRequest(suspMember);
                res = sendClientIP(ClientIP);
                break;
        }
        return res;
    }

    public Response send(Relateds relateds) {
        Response res = null;
        switch (relateds.getPMembertype()) {
            case 1:
                SetClientUL ClientUL = relatedsMapper.toUlRequest(relateds);
                res = sendClientUL(ClientUL);
                break;
            case 2:
                SetClientFL ClientFL = relatedsMapper.toFlRequest(relateds);
                res = sendClientFL(ClientFL);
                break;
            case 3:
                SetClientIP ClientIP = relatedsMapper.toIPRequest(relateds);
                res = sendClientIP(ClientIP);
                break;
        }
        return res;
    }

    public Response send(ClientLinkRequest clientLinkRequest) {
        Response res = sendClientRole(clientLinkRequest);
        return res;
    }

    public String sendRelateds(Suspiciousmembers suspMember, Long clientId) {

        String sql = """
                select a.id,
                       a.client_card_id as p_suspiciousmemberid,
                       coalesce(b.p_membertype, 2) as p_membertype,
                	   coalesce(b.bs_id, 99) as bs_id,
                       3 as role_id,
                       a.name,
                       a.first_name,
                       a.last_name,
                       a.middle_name,
                       to_char(null) as p_founderopf,
                       coalesce(to_char(a.iin), b.p_clientid) iin,
                       coalesce(coalesce(b.p_bsclientid, coalesce(to_char(a.iin), b.p_clientid)), to_char(a.id)) as  p_bsclientid,
                       coalesce(b.p_bank_client,1) as p_bank_client,
                       coalesce(coalesce(a.countrycode, a.p_rescountrycode), b.p_memberrescountrycode) as p_memberrescountrycode,
                       coalesce(a.p_citizenship, b.p_memberrescitizenship) as p_memberrescitizenship,
                       b.p_membertaxcode,
                       b.p_membermaincode,
                       b.p_memberphone,
                       b.p_memberemail,
                       coalesce(b.p_memberacdoctypecode, a.doc_type) as p_memberacdoctypecode,
                       b.p_memberacdocseries,
                       coalesce(a.doc_number, b.p_memberacdocnumber) as p_memberacdocnumber,\s
                       coalesce(a.doc_issued_by, b.p_memberacdocwhom) as p_memberacdocwhom,\s
                       coalesce(a.doc_issue_date, b.p_memberacdocissuedate) as p_memberacdocissuedate,
                       coalesce(a.birth_date, b.p_memberacbirthdate) as p_memberacbirthdate,
                       b.p_memberacbirthplace,
                       b.p_branch,
                       b.p_e_sector,
                       b.p_emp_bank,
                       b.p_username,
                       b.p_approve,
                       b.p_memberregfulladdress,
                       b.p_memberregcountrycode,
                       b.p_memberregarea,
                       b.p_memberregregion,
                       b.p_memberregcity,
                       b.p_memberregstreet,
                       b.p_memberreghouse,
                       b.p_memberregoffice,
                       b.p_memberregpostcode,
                       b.p_memberseatfulladdress,
                       b.p_memberseatcountrycode,
                       b.p_memberseatarea,
                       b.p_memberseatregion,
                       b.p_memberseatcity,
                       b.p_memberseatstreet,
                       b.p_memberseathouse,
                       b.p_memberseatoffice,
                       b.p_memberseatpostcode,
                       b.reg_kato_id,
                       b.seat_kato_id,
                       b.reg_area_kato_id,
                       b.seat_area_kato_id,
                       b.reg_region_kato_id,
                       b.seat_region_kato_id 
                from %s.beneficial_owners a
                left join (
                    select b.*,
                           row_number() over (
                               partition by b.p_membermaincode, b.p_membertype
                               order by b.id
                           ) as rn
                    from %s.tb_suspiciousmembers b
                    where b.p_membertype = 2
                ) b
                    on b.p_membermaincode = to_char(a.iin)
                   and b.p_membertype = 2
                   and b.rn = 1
                   where a.client_card_id = ?                
                """.formatted(oracleSchemaUser, oracleSchemaUser);;
        ClientLinkRequest clientLinkRequest = new ClientLinkRequest();
        Response res;
        List<Relateds> beneficial_owners = jdbcTemplate.query(
                sql,
                new BeanPropertyRowMapper<>(Relateds.class),
                suspMember.getId()
        );
        log.info("Processing beneficial_owners of client {} numbers of them {}", suspMember.getPClientid(), beneficial_owners.size());

        for (Relateds relateds : beneficial_owners) {
            res = send(relateds);
            log.info("Processing beneficial_owners name: {}, iin {}", relateds.getName() ,relateds.getIin());
            if(res != null && res.getInId() != null && res.getInId() > 0) {
                clientLinkRequest.setClientId(clientId);
                clientLinkRequest.setRoleId(relateds.getRoleId());
                clientLinkRequest.setLinkedClientId(res.getInId());
                clientLinkRequest.setLinkStatus(1);
                send(clientLinkRequest);
            } else {
                log.error("Error sending client name"+ relateds.getName());
            }
        }
        clientLinkRequest = new ClientLinkRequest();
        res = null;
        sql = """                
              select a.id,
                  a.p_suspiciousmemberid,
                  2 as role_id,
                  coalesce(a.p_foundertype, to_char(b.p_membertype)) as p_membertype,
                  coalesce(b.bs_id, 99) as bs_id,
                  a.p_founderurname as name,
                  a.p_founder_urfirstname as first_name,
                  a.p_founder_urlastname as last_name,
                  a.p_founder_urmiddlename as middle_name,
                  a.p_founderopf,
                  b.p_clientid as iin,
                  coalesce(coalesce(b.p_bsclientid, b.p_clientid), to_char(a.id)) as p_bsclientid,
                  coalesce(b.p_bank_client,1) as p_bank_client,
                  coalesce(a.p_founderurrescountrycode, b.p_memberrescountrycode) as p_memberrescountrycode,
                  b.p_memberrescitizenship,
                  b.p_membertaxcode,
                  b.p_membermaincode,
                  b.p_memberphone,
                  b.p_memberemail,
                  b.p_memberacdoctypecode,
                  b.p_memberacdocseries,
                  b.p_memberacdocnumber,
                  b.p_memberacdocwhom,
                  b.p_memberacdocissuedate,
                  b.p_memberacbirthdate,
                  b.p_memberacbirthplace,
                  b.p_branch,
                  b.p_e_sector,
                  b.p_emp_bank,
                  b.p_username,
                  b.p_approve,
                  b.p_memberregfulladdress,
                  b.p_memberregcountrycode,
                  b.p_memberregarea,
                  b.p_memberregregion,
                  b.p_memberregcity,
                  b.p_memberregstreet,
                  b.p_memberreghouse,
                  b.p_memberregoffice,
                  b.p_memberregpostcode,
                  b.p_memberseatfulladdress,
                  b.p_memberseatcountrycode,
                  b.p_memberseatarea,
                  b.p_memberseatregion,
                  b.p_memberseatcity,
                  b.p_memberseatstreet,
                  b.p_memberseathouse,
                  b.p_memberseatoffice,
                  b.p_memberseatpostcode,
                  b.reg_kato_id,
                  b.seat_kato_id,
                  b.reg_area_kato_id,
                  b.seat_area_kato_id,
                  b.reg_region_kato_id,
                  b.seat_region_kato_id 
           from %s.tb_SuspiciousFounders a
           left join (
               select b.*,
                      row_number() over (
                          partition by b.p_memberurname, b.p_membertype
                          order by b.rowid
                      ) as rn
               from %s.tb_suspiciousmembers b
           ) b
               on b.p_memberurname = to_char(a.p_founderurname)
              and b.p_membertype = a.p_foundertype
              and b.rn = 1
                   where a.p_suspiciousmemberid = ?                
                """.formatted(oracleSchemaUser, oracleSchemaUser);
        List<Relateds> SuspiciousFounders = jdbcTemplate.query(
                sql,
                new BeanPropertyRowMapper<>(Relateds.class),
                suspMember.getId()
        );
        log.info("Processing SuspiciousFounders of client {} numbers of them {}", suspMember.getPClientid(), SuspiciousFounders.size());
        for (Relateds relateds : SuspiciousFounders) {
            res = send(relateds);
            if(res != null && res.getInId() != null && res.getInId() > 0) {
                clientLinkRequest.setClientId(clientId);
                clientLinkRequest.setRoleId(relateds.getRoleId());
                clientLinkRequest.setLinkedClientId(res.getInId());
                clientLinkRequest.setLinkStatus(1);
                send(clientLinkRequest);
            } else {
                log.error("Error sending client name"+ relateds.getName());
            }
            log.info("Processing SuspiciousFounders name: {}, iin {}", relateds.getName() ,relateds.getIin());
        }
        return "";
    }
}