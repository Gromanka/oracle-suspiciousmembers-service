package com.example.demo.controller;

import com.example.demo.dto.OffMember;
import com.example.demo.dto.OfflineOperationDto;
import com.example.demo.dto.Response;
import com.example.demo.dto.Suspiciousmembers;
import com.example.demo.service.ClientCardService;
import com.example.demo.service.OfflineOperationsMigr;
import com.example.demo.service.ProcessRisks;
import com.example.demo.service.SuspMembersMigr;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/clients")
public class ClientsController {

    private final JdbcTemplate jdbcTemplate;
    private final ClientCardService clientCardService;
    private final SuspMembersMigr suspMembersMigr;
    private final ProcessRisks processRisks;

    @Value("${spring.datasource.oracle.schema:${DB_SCHEMA_USER:AML_USER_ALTYN}}")
    private String oracleSchemaUser;

    @Value("${saveRiskFromOracle:${SAVE_RISK_FROM_ORACLE:true}}")
    private boolean saveRiskFromOracle;

    public ClientsController(@Qualifier("oracleJdbcTemplate") JdbcTemplate jdbcTemplate, ClientCardService clientCardService, SuspMembersMigr suspMembersMigr, ProcessRisks processRisks) {
        this.jdbcTemplate = jdbcTemplate;
        this.clientCardService = clientCardService;
        this.suspMembersMigr = suspMembersMigr;
        this.processRisks = processRisks;
    }


    @PostMapping("/setSingleClientByBsClientsIdMemberType")
    public Response setSingleClientByBsClientsIdMemberType(String p_bsclientid, Integer pMembertype){
        String sql = """
            SELECT *
            FROM %s.tb_suspiciousmembers
            WHERE p_bsclientid = ? and p_membertype = ?
            """.formatted(oracleSchemaUser);

        Suspiciousmembers client = jdbcTemplate.queryForObject(
                sql,
                new BeanPropertyRowMapper<>(Suspiciousmembers.class),
                p_bsclientid,
                pMembertype
        );
        if (client == null) {
            return new Response();
        }
        Response response =  clientCardService.send(client);
        if(saveRiskFromOracle) {
            processRisks.procesRisk(client.getId(), response.getInId());
        }

        return response;
    }

    @PostMapping("/setClientsAll")
    public Response setClientsAll() {

        int batchSize = 500;
        long lastId = 0;

        while (true) {

            String sql = """
            SELECT *
            FROM (
                SELECT *
                FROM %s.tb_suspiciousmembers
                WHERE id > ?
                ORDER BY id ASC
            )
            WHERE ROWNUM <= ?
            """.formatted(oracleSchemaUser);
            List<Suspiciousmembers> clients = jdbcTemplate.query(
                    sql,
                    new BeanPropertyRowMapper<>(Suspiciousmembers.class),
                    lastId,
                    batchSize
            );

            if (clients.isEmpty()) {
                break;
            }

            suspMembersMigr.processClient(clients);

            lastId = clients.get(clients.size() - 1).getId();
        }

        return new Response();
    }
}
