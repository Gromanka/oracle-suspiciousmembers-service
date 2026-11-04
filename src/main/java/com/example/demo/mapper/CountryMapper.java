package com.example.demo.mapper;

import com.example.demo.dto.Country;
import com.example.demo.dto.Suspiciousmembers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Component
public class CountryMapper {

    private final Map<String, String> countryCodes;

    public CountryMapper(
            @Qualifier("oracleJdbcTemplate") JdbcTemplate jdbcTemplate,
            @Value("${spring.datasource.oracle.schema:AML_USER_ALTYN}")
            String oracleSchemaUser) {

        String sql = """
                select p_code, p_code_str2
                from %s.tb_dict_country
                where p_code_str2 is not null
                  and p_code is not null
                """.formatted(oracleSchemaUser);

        List<Country> countries = jdbcTemplate.query(
                sql,
                new BeanPropertyRowMapper<>(Country.class)
        );

        this.countryCodes = countries.stream()
                .collect(Collectors.toMap(
                        Country::getP_code,
                        Country::getP_code_str2,
                        (existing, replacement) -> existing
                ));
    }

    public String getCountryCode2(String pCode) {
        if (pCode == null || pCode.isBlank()) {
            return null;
        }

        return countryCodes.get(pCode.trim());
    }
}