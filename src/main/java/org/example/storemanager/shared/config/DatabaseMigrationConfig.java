package org.example.storemanager.shared.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import javax.sql.DataSource;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.migration.enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseMigrationConfig {

    private final DataSource dataSource;

    @Bean
    CommandLineRunner runMigrations() {
        return args -> {
            try {
                ClassPathResource schemaResource = new ClassPathResource("schema.sql");
                if (schemaResource.exists()) {
                    log.info("[Migration] Executing database migration schema.sql...");
                    ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
                    populator.addScript(schemaResource);
                    populator.setContinueOnError(true);
                    populator.execute(dataSource);
                    log.info("[Migration] Database migration scripts executed successfully.");
                } else {
                    log.info("[Migration] No schema.sql found, skipping migration.");
                }
            } catch (Exception e) {
                log.warn("[Migration] Database migration encountered an issue (non-fatal): {}", e.getMessage());
            }
        };
    }
}
