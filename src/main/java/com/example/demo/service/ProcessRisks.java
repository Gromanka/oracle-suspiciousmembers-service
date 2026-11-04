package com.example.demo.service;

import com.example.demo.dto.RiskCardDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ProcessRisks {

    @Value("${spring.datasource.oracle.schema:${DB_SCHEMA_USER:AML_USER_ALTYN}}")
    private String oracleSchemaUser;

    private final JdbcTemplate oracleJdbcTemplate;
    private final JdbcTemplate postgresJdbcTemplate;

    public ProcessRisks(@Qualifier("oracleJdbcTemplate") JdbcTemplate oracleJdbcTemplate,
                        @Qualifier("postgresJdbcTemplate") JdbcTemplate postgresJdbcTemplate) {
        this.oracleJdbcTemplate = oracleJdbcTemplate;
        this.postgresJdbcTemplate = postgresJdbcTemplate;
    }

    public void procesRisk(Long oracleId, Long cogniveId) {
        if (oracleId == null || cogniveId == null) {
            log.error("Invalid arguments: oracleId = {}, cogniveId = {}", oracleId, cogniveId);
            return;
        }

        long start = System.currentTimeMillis();
        log.info("Start processing Risk Card oracleId = {}, cogniveId = {}", oracleId, cogniveId);

        try {
            Optional<RiskCardDto> dto = loadRiskCardFromOracle(oracleId);
            if (dto.isEmpty()) {
                log.error("Risk Card processing skipped: card not loaded from Oracle, oracleId = {}, cogniveId = {}, took {} ms",
                        oracleId, cogniveId, System.currentTimeMillis() - start);
                return;
            }

            boolean saved = saveRiskCard(dto.get(), cogniveId);
            if (!saved) {
                log.error("Risk Card processing failed: not saved to Postgres, oracleId = {}, cogniveId = {}, took {} ms",
                        oracleId, cogniveId, System.currentTimeMillis() - start);
                return;
            }

            log.info("Risk Card processed successfully oracleId = {}, cogniveId = {}, took {} ms",
                    oracleId, cogniveId, System.currentTimeMillis() - start);

        } catch (Exception e) {
            log.error("Unexpected error while processing Risk Card oracleId = {}, cogniveId = {}, took {} ms",
                    oracleId, cogniveId, System.currentTimeMillis() - start, e);
        }
    }

    private Optional<RiskCardDto> loadRiskCardFromOracle(Long oracleId) {
        String sql = """
                SELECT *
                FROM %s.RISK_CARD
                WHERE card_id = ?
                """.formatted(oracleSchemaUser);

        log.debug("Loading Risk Card from Oracle, schema = {}, oracleId = {}", oracleSchemaUser, oracleId);

        try {
            List<RiskCardDto> rows = oracleJdbcTemplate.query(
                    sql,
                    new BeanPropertyRowMapper<>(RiskCardDto.class),
                    oracleId
            );

            if (rows.isEmpty()) {
                log.error("Risk Card not found in Oracle, oracleId = {}", oracleId);
                return Optional.empty();
            }

            if (rows.size() > 1) {
                log.warn("Found {} rows in Oracle for card_id = {}, using the first one", rows.size(), oracleId);
            }

            log.debug("Risk Card loaded from Oracle, oracleId = {}", oracleId);
            return Optional.of(rows.get(0));

        } catch (Exception e) {
            log.error("Failed to read Risk Card from Oracle, oracleId = {}", oracleId, e);
            return Optional.empty();
        }
    }

    /**
     * @return true, если запись успешно сохранена в Postgres, иначе false (ошибка уже залогирована)
     */
    public boolean saveRiskCard(RiskCardDto dto, Long cogniveId) {
        if (dto == null) {
            log.error("Cannot save Risk Card: dto is null, cogniveId = {}", cogniveId);
            return false;
        }

        log.debug("Saving Risk Card to Postgres, cogniveId = {}", cogniveId);

        String sql = """
                INSERT INTO public.risk_card (
                    card_id,
                    stat_org_form,
                    stat_reg_country,
                    stat_subject_type,
                    stat_oked,
                    stat_iin_bin,
                    stat_industry,
                    stat_telephone,
                    stat_email,
                    stat_adr_ur_country,
                    stat_adr_ur_city,
                    stat_adr_ur_street,
                    stat_adr_ur_home,
                    stat_adr_ur_office,
                    stat_adr_ur_post_index,
                    stat_adr_fact_country,
                    stat_adr_fact_city,
                    stat_adr_fact_street,
                    stat_adr_fact_home,
                    stat_adr_fact_office,
                    stat_adr_fact_post_index,
                    stat_doc_type,
                    stat_doc_int,
                    stat_doc_issued_by,
                    stat_doc_issued_date,
                    stat_risk_ball,
                    messages_fm,
                    messages_susp,
                    messages_ball,
                    product_ball,
                    expert_solution,
                    expert_ball,
                    expert_date,
                    expert_remark,
                    summary_ball,
                    summary_risk,
                    summary_remark,
                    summary_date,
                    stat_suspicious_bool,
                    stat_is_fict_company,
                    stat_accountmoneysources,
                    stat_mainbusinesstype,
                    stat_mainincomesources,
                    stat_govlicense,
                    stat_emp_bank,
                    product_remark,
                    messages_fm_remark,
                    messages_susp_remark,
                    calc_remark,
                    stat_citizenship
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
                )
                ON CONFLICT (card_id) DO UPDATE SET
                    stat_org_form = EXCLUDED.stat_org_form,
                    stat_reg_country = EXCLUDED.stat_reg_country,
                    stat_subject_type = EXCLUDED.stat_subject_type,
                    stat_oked = EXCLUDED.stat_oked,
                    stat_iin_bin = EXCLUDED.stat_iin_bin,
                    stat_industry = EXCLUDED.stat_industry,
                    stat_telephone = EXCLUDED.stat_telephone,
                    stat_email = EXCLUDED.stat_email,
                    stat_adr_ur_country = EXCLUDED.stat_adr_ur_country,
                    stat_adr_ur_city = EXCLUDED.stat_adr_ur_city,
                    stat_adr_ur_street = EXCLUDED.stat_adr_ur_street,
                    stat_adr_ur_home = EXCLUDED.stat_adr_ur_home,
                    stat_adr_ur_office = EXCLUDED.stat_adr_ur_office,
                    stat_adr_ur_post_index = EXCLUDED.stat_adr_ur_post_index,
                    stat_adr_fact_country = EXCLUDED.stat_adr_fact_country,
                    stat_adr_fact_city = EXCLUDED.stat_adr_fact_city,
                    stat_adr_fact_street = EXCLUDED.stat_adr_fact_street,
                    stat_adr_fact_home = EXCLUDED.stat_adr_fact_home,
                    stat_adr_fact_office = EXCLUDED.stat_adr_fact_office,
                    stat_adr_fact_post_index = EXCLUDED.stat_adr_fact_post_index,
                    stat_doc_type = EXCLUDED.stat_doc_type,
                    stat_doc_int = EXCLUDED.stat_doc_int,
                    stat_doc_issued_by = EXCLUDED.stat_doc_issued_by,
                    stat_doc_issued_date = EXCLUDED.stat_doc_issued_date,
                    stat_risk_ball = EXCLUDED.stat_risk_ball,
                    messages_fm = EXCLUDED.messages_fm,
                    messages_susp = EXCLUDED.messages_susp,
                    messages_ball = EXCLUDED.messages_ball,
                    product_ball = EXCLUDED.product_ball,
                    expert_solution = EXCLUDED.expert_solution,
                    expert_ball = EXCLUDED.expert_ball,
                    expert_date = EXCLUDED.expert_date,
                    expert_remark = EXCLUDED.expert_remark,
                    summary_ball = EXCLUDED.summary_ball,
                    summary_risk = EXCLUDED.summary_risk,
                    summary_remark = EXCLUDED.summary_remark,
                    summary_date = EXCLUDED.summary_date,
                    stat_suspicious_bool = EXCLUDED.stat_suspicious_bool,
                    stat_is_fict_company = EXCLUDED.stat_is_fict_company,
                    stat_accountmoneysources = EXCLUDED.stat_accountmoneysources,
                    stat_mainbusinesstype = EXCLUDED.stat_mainbusinesstype,
                    stat_mainincomesources = EXCLUDED.stat_mainincomesources,
                    stat_govlicense = EXCLUDED.stat_govlicense,
                    stat_emp_bank = EXCLUDED.stat_emp_bank,
                    product_remark = EXCLUDED.product_remark,
                    messages_fm_remark = EXCLUDED.messages_fm_remark,
                    messages_susp_remark = EXCLUDED.messages_susp_remark,
                    calc_remark = EXCLUDED.calc_remark,
                    stat_citizenship = EXCLUDED.stat_citizenship
                """;

        try {
            int affected = postgresJdbcTemplate.update(
                    sql,
                    cogniveId,                          // card_id
                    dto.getStatOrgForm(),               // stat_org_form
                    dto.getStatRegCountry(),            // stat_reg_country
                    dto.getStatSubjectType(),           // stat_subject_type
                    dto.getStatOked(),                  // stat_oked
                    dto.getStatIinBin(),                // stat_iin_bin
                    dto.getStatIndustry(),              // stat_industry
                    dto.getStatTelephone(),             // stat_telephone
                    dto.getStatEmail(),                 // stat_email
                    dto.getStatAdrUrCountry(),          // stat_adr_ur_country
                    dto.getStatAdrUrCity(),             // stat_adr_ur_city
                    dto.getStatAdrUrStreet(),           // stat_adr_ur_street
                    dto.getStatAdrUrHome(),             // stat_adr_ur_home
                    dto.getStatAdrUrOffice(),           // stat_adr_ur_office
                    dto.getStatAdrUrPostIndex(),        // stat_adr_ur_post_index
                    dto.getStatAdrFactCountry(),        // stat_adr_fact_country
                    dto.getStatAdrFactCity(),           // stat_adr_fact_city
                    dto.getStatAdrFactStreet(),         // stat_adr_fact_street
                    dto.getStatAdrFactHome(),           // stat_adr_fact_home
                    dto.getStatAdrFactOffice(),         // stat_adr_fact_office
                    dto.getStatAdrFactPostIndex(),      // stat_adr_fact_post_index
                    dto.getStatDocType(),               // stat_doc_type
                    dto.getStatDocNumber(),             // stat_doc_int
                    dto.getStatDocIssuedBy(),           // stat_doc_issued_by
                    dto.getStatDocIssuedDate(),         // stat_doc_issued_date
                    dto.getStatRiskBall(),              // stat_risk_ball
                    dto.getMessagesFm(),                // messages_fm
                    dto.getMessagesSusp(),              // messages_susp
                    dto.getMessagesBall(),              // messages_ball
                    dto.getProductBall(),               // product_ball
                    dto.getExpertSolution(),            // expert_solution
                    dto.getExpertBall(),                // expert_ball
                    dto.getExpertDate(),                // expert_date
                    dto.getExpertRemark(),              // expert_remark
                    dto.getSummaryBall(),               // summary_ball
                    dto.getSummaryRisk(),               // summary_risk
                    dto.getSummaryRemark(),             // summary_remark
                    dto.getSummaryDate(),               // summary_date
                    dto.getStatSuspiciousBool(),        // stat_suspicious_bool
                    dto.getStatIsFictCompany(),         // stat_is_fict_company
                    dto.getStatAccountmoneysources(),   // stat_accountmoneysources
                    dto.getStatMainbusinesstype(),      // stat_mainbusinesstype
                    dto.getStatMainincomesources(),     // stat_mainincomesources
                    dto.getStatGovlicense(),            // stat_govlicense
                    dto.getStatEmpBank(),               // stat_emp_bank
                    dto.getProductRemark(),             // product_remark
                    dto.getMessagesFmRemark(),          // messages_fm_remark
                    dto.getMessagesSuspRemark(),        // messages_susp_remark
                    dto.getCalcRemark(),                // calc_remark
                    dto.getStatRegCitizen()             // stat_citizenship
            );

            log.info("Risk Card saved to Postgres, cogniveId = {}, affected rows = {}", cogniveId, affected);
            return true;

        } catch (Exception e) {
            log.error("Failed to save Risk Card to Postgres, cogniveId = {}", cogniveId, e);
            return false;
        }
    }
}