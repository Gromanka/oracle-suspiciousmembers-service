package com.example.demo.service;

import com.example.demo.dto.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

@Service
@Slf4j
public class SuspMembersMigr {
    private final JdbcTemplate jdbcTemplate;
    private final ClientCardService clientCardService;
    private final ProcessRisks processRisks;


    @Value("${saveRiskFromOracle:${SAVE_RISK_FROM_ORACLE:true}}")
    private boolean saveRiskFromOracle;

    public SuspMembersMigr(@Qualifier("oracleJdbcTemplate")JdbcTemplate jdbcTemplate, ClientCardService clientCardService, ProcessRisks processRisks) {
        this.jdbcTemplate = jdbcTemplate;
        this.clientCardService = clientCardService;
        this.processRisks = processRisks;
    }

    public String processClient(List<Suspiciousmembers> dto) {

        log.info("Processing clients: {}", dto.size());

        int successCount = 0;
        int errorCount = 0;

        for (Suspiciousmembers mem : dto) {
            try {
                log.info("Processing single client IIN: {}", mem.getPClientid());
                Response res = clientCardService.send(mem);
                if (res == null || res.getInId() == null || res.getInId() <= 0) {
                    log.error("Error sending client IIN: {}, response: {}", mem.getPClientid(), res);
                    errorCount++;
                    continue;
                }
                // Сохранение рисков из Oracle
                if (saveRiskFromOracle) {
                    try {
                        processRisks.procesRisk(mem.getId(), res.getInId());
                    } catch (Exception e) {
                        log.error("Error processing risks for client IIN: {}", mem.getPClientid(), e);
                    }
                }

                // Отправка связанных клиентов
                try {
                    clientCardService.sendRelateds(mem, res.getInId());
                } catch (Exception e) {
                    log.error("Error sending related clients for IIN: {}", mem.getPClientid(), e);
                }
                successCount++;
                log.info("End processing single client IIN: {}, inId: {}", mem.getPClientid(), res.getInId());
            } catch (Exception e) {
                errorCount++;
                log.error("Unexpected error processing client IIN: {}", mem.getPClientid(), e);
            }
        }

        log.info("Processing completed. Total: {}, Success: {}, Errors: {}",
                dto.size(), successCount, errorCount);

        return String.format(
                "Process %d clients: Success=%d, Errors=%d",
                dto.size(), successCount, errorCount
        );
    }
}
