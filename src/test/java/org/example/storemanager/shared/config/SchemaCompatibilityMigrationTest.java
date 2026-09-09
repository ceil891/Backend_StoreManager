package org.example.storemanager.shared.config;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataAccessResourceFailureException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class SchemaCompatibilityMigrationTest {
    private static final String RETURNS_SQL =
            "ALTER TABLE IF EXISTS customer_returns ALTER COLUMN invoice_id DROP NOT NULL";

    @Test
    void startupMigratesExistingInvoiceConstraint() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        new SchemaCompatibilityMigration(jdbc).apply();
        verify(jdbc).execute(RETURNS_SQL);
    }

    @Test
    void requiredReturnMigrationFailureIsNotSilentlyIgnored() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        doThrow(new DataAccessResourceFailureException("Migration failed"))
                .when(jdbc).execute(RETURNS_SQL);
        assertThatThrownBy(() -> new SchemaCompatibilityMigration(jdbc).apply())
                .isInstanceOf(DataAccessResourceFailureException.class);
    }
}
