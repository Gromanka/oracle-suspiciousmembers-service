package com.example.demo.controller;

import com.example.demo.dto.OffMember;
import com.example.demo.dto.OfflineOperationDto;
import com.example.demo.dto.Response;
import com.example.demo.service.OfflineOperationsMigr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/operations")
public class OfflineOperationsController {

    private static final int BATCH_SIZE = 200;
    private static final int ORACLE_IN_LIMIT = 1000;

    private final JdbcTemplate jdbcTemplate;
    private final OfflineOperationsMigr offlineOperationsMigr;

    private final BeanPropertyRowMapper<OfflineOperationDto> opMapper =
            new BeanPropertyRowMapper<>(OfflineOperationDto.class);
    private final BeanPropertyRowMapper<OffMember> memberMapper =
            new BeanPropertyRowMapper<>(OffMember.class);

    @Value("${spring.datasource.oracle.schema:${DB_SCHEMA_USER:AML_USER_ALTYN}}")
    private String oracleSchemaUser;

    public OfflineOperationsController(@Qualifier("oracleJdbcTemplate") JdbcTemplate jdbcTemplate,
                                       OfflineOperationsMigr offlineOperationsMigr) {
        this.jdbcTemplate = jdbcTemplate;
        this.offlineOperationsMigr = offlineOperationsMigr;
    }

    /** Результат обработки одного дня */
    private record DayResult(Response response, int batches, long operations, long members) {}

    @PostMapping("/loadOfflineOPerations")
    public Response loadOfflineOPerations(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateEnd
    ) {
        if (dateStart.isAfter(dateEnd)) {
            throw new IllegalArgumentException("dateStart не может быть больше dateEnd");
        }

        final long totalStartedAt = System.nanoTime();

        log.info("Начало загрузки offline operations: dateStart={}, dateEnd={}, batchSize={}",
                dateStart, dateEnd, BATCH_SIZE);

        Response lastResponse = new Response();
        long totalOperations = 0;
        long totalMembers = 0;
        int totalBatches = 0;
        int daysProcessed = 0;
        LocalDate currentDay = dateStart;

        try {
            // Идём день за днём: [dateStart ... dateEnd] включительно
            for (; !currentDay.isAfter(dateEnd); currentDay = currentDay.plusDays(1)) {
                long dayStartedAt = System.nanoTime();

                log.info("=== День {}: начало обработки ===", currentDay);

                DayResult dayResult = processDay(currentDay);

                if (dayResult.response() != null) {
                    lastResponse = dayResult.response();
                }
                totalOperations += dayResult.operations();
                totalMembers += dayResult.members();
                totalBatches += dayResult.batches();
                daysProcessed++;

                log.info("=== День {}: завершён, batches={}, operations={}, members={}, dayMs={} ===",
                        currentDay,
                        dayResult.batches(),
                        dayResult.operations(),
                        dayResult.members(),
                        (System.nanoTime() - dayStartedAt) / 1_000_000);
            }

            log.info("Загрузка offline operations завершена: dateStart={}, dateEnd={}, days={}, batches={}, totalOperations={}, totalMembers={}, totalMs={}",
                    dateStart, dateEnd, daysProcessed, totalBatches,
                    totalOperations, totalMembers,
                    (System.nanoTime() - totalStartedAt) / 1_000_000);

            return lastResponse;

        } catch (Exception e) {
            log.error("Ошибка загрузки offline operations: dateStart={}, dateEnd={}, failedDay={}, daysProcessed={}, totalOperations={}, totalMembers={}, elapsedMs={}",
                    dateStart, dateEnd, currentDay, daysProcessed,
                    totalOperations, totalMembers,
                    (System.nanoTime() - totalStartedAt) / 1_000_000,
                    e);
            throw e;
        }
    }

    /** Загрузка и миграция всех операций за один день (пагинация по ключу внутри дня) */
    private DayResult processDay(LocalDate day) {
        final Timestamp start = Timestamp.valueOf(day.atStartOfDay());
        final Timestamp end = Timestamp.valueOf(day.plusDays(1).atStartOfDay());

        String firstSql = """
                SELECT * FROM (
                    SELECT o.*
                    FROM %s.TB_OFFLINEOPERATIONS o
                    WHERE o.P_OPERATIONDATETIME >= ?
                      AND o.P_OPERATIONDATETIME < ?
                    ORDER BY o.P_OPERATIONDATETIME, o.ID
                ) WHERE ROWNUM <= ?
                """.formatted(oracleSchemaUser);

        String nextSql = """
                SELECT * FROM (
                    SELECT o.*
                    FROM %s.TB_OFFLINEOPERATIONS o
                    WHERE o.P_OPERATIONDATETIME >= ?
                      AND o.P_OPERATIONDATETIME < ?
                      AND (
                          o.P_OPERATIONDATETIME > ?
                          OR (o.P_OPERATIONDATETIME = ? AND o.ID > ?)
                      )
                    ORDER BY o.P_OPERATIONDATETIME, o.ID
                ) WHERE ROWNUM <= ?
                """.formatted(oracleSchemaUser);

        String membersSqlTemplate = """
                SELECT *
                FROM %s.TB_OFF_MEMBERS
                WHERE P_OPERATIONID IN (%%s)
                ORDER BY P_OPERATIONID, ID
                """.formatted(oracleSchemaUser);

        Response response = null;
        Timestamp lastDateTime = null;
        Long lastId = null;

        int batchNumber = 0;
        long dayOperations = 0;
        long dayMembers = 0;

        while (true) {
            batchNumber++;
            long batchStartedAt = System.nanoTime();

            // 1. Очередной пакет операций
            long queryStartedAt = System.nanoTime();

            List<OfflineOperationDto> operations;
            if (lastId == null) {
                operations = jdbcTemplate.query(firstSql, opMapper, start, end, BATCH_SIZE);
            } else {
                operations = jdbcTemplate.query(nextSql, opMapper,
                        start, end, lastDateTime, lastDateTime, lastId, BATCH_SIZE);
            }

            long queryMs = (System.nanoTime() - queryStartedAt) / 1_000_000;

            if (operations.isEmpty()) {
                log.info("День {}, пакет {}: операции не найдены, oracleQueryMs={}",
                        day, batchNumber, queryMs);
                batchNumber--; // пустой пакет не считаем
                break;
            }

            OfflineOperationDto last = operations.get(operations.size() - 1);
            lastId = last.getId();
            lastDateTime = Timestamp.valueOf(last.getPOperationdatetime());

            dayOperations += operations.size();

            log.info("День {}, пакет {}: загружено операций={}, oracleQueryMs={}, lastDateTime={}, lastId={}",
                    day, batchNumber, operations.size(), queryMs, lastDateTime, lastId);

            // 2. ID операций
            List<Long> operationIds = operations.stream()
                    .map(OfflineOperationDto::getId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            // 3. Участники чанками
            Map<Long, List<OffMember>> membersByOperation = new HashMap<>();
            int memberChunks = 0;
            int loadedMembersInBatch = 0;
            long membersStartedAt = System.nanoTime();

            for (int i = 0; i < operationIds.size(); i += ORACLE_IN_LIMIT) {
                List<Long> chunk = operationIds.subList(
                        i, Math.min(i + ORACLE_IN_LIMIT, operationIds.size()));

                String placeholders = String.join(",", Collections.nCopies(chunk.size(), "?"));
                String membersSql = membersSqlTemplate.formatted(placeholders);

                memberChunks++;

                List<OffMember> members = jdbcTemplate.query(membersSql, memberMapper, chunk.toArray());
                loadedMembersInBatch += members.size();

                for (OffMember member : members) {
                    membersByOperation
                            .computeIfAbsent(member.getPOperationid(), k -> new ArrayList<>())
                            .add(member);
                }
            }

            dayMembers += loadedMembersInBatch;

            log.info("День {}, пакет {}: участников={}, операций для поиска={}, SQL чанков={}, membersQueryMs={}",
                    day, batchNumber, loadedMembersInBatch, operationIds.size(), memberChunks,
                    (System.nanoTime() - membersStartedAt) / 1_000_000);

            // 4. Привязка участников
            for (OfflineOperationDto operation : operations) {
                operation.setMembers(
                        membersByOperation.getOrDefault(operation.getId(), Collections.emptyList()));
            }

            // 5. Миграция пакета
            long migrationStartedAt = System.nanoTime();

            log.info("День {}, пакет {}: начало миграции, операций={}, участников={}",
                    day, batchNumber, operations.size(), loadedMembersInBatch);

            response = offlineOperationsMigr.LoadOperations(operations);

            log.info("День {}, пакет {}: миграция завершена, migrationMs={}, batchMs={}",
                    day, batchNumber,
                    (System.nanoTime() - migrationStartedAt) / 1_000_000,
                    (System.nanoTime() - batchStartedAt) / 1_000_000);

            // Неполный пакет — данные за день закончились
            if (operations.size() < BATCH_SIZE) {
                log.info("День {}, пакет {}: размер {} < batchSize {}, день прочитан",
                        day, batchNumber, operations.size(), BATCH_SIZE);
                break;
            }
        }

        return new DayResult(response, batchNumber, dayOperations, dayMembers);
    }
}