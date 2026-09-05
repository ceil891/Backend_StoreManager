package org.example.storemanager.modules.common.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping({"/", "/health"})
    public ResponseEntity<Map<String, String>> healthCheck() {
        String dbStatus = "UP";
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        } catch (Exception e) {
            log.warn("Database keepalive ping warning: {}", e.getMessage());
            dbStatus = "DEGRADED";
        }

        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "database", dbStatus,
                "service", "StoreManager Backend"
        ));
    }
}
