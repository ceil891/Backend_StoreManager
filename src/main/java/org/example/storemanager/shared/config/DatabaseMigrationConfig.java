package org.example.storemanager.shared.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * DatabaseMigrationRunner - Runs safe idempotent ALTER TABLE migrations on startup.
 * Uses IF NOT EXISTS so it's safe to run multiple times.
 * Order(1) ensures it runs first among ApplicationRunners.
 */
@Slf4j
@Component
@Lazy(false)
@Order(1)
@RequiredArgsConstructor
public class DatabaseMigrationConfig implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        log.info("[Migration] Database migration check skipped (schema is managed/validated externally).");
    }
}

