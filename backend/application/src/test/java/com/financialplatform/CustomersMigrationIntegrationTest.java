package com.financialplatform;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class CustomersMigrationIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("financial_platform")
            .withUsername("financial")
            .withPassword("financial");

    @Test
    void shouldApplyCustomersMigrationWithUniqueDocumentConstraint() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword())) {

            runSql(connection, "CREATE EXTENSION IF NOT EXISTS \"pgcrypto\";");
            runSql(connection, """
                    CREATE TABLE IF NOT EXISTS customers (
                        id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        name        VARCHAR(255) NOT NULL,
                        type        VARCHAR(20)  NOT NULL,
                        document    VARCHAR(14)  NOT NULL,
                        email       VARCHAR(255) NOT NULL,
                        created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                        created_by  VARCHAR(100) NOT NULL,
                        updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                        updated_by  VARCHAR(100) NOT NULL,
                        CONSTRAINT uk_customers_document UNIQUE (document)
                    );
                    """);

            runSql(connection, """
                    INSERT INTO customers (id, name, type, document, email, created_by, updated_by)
                    VALUES (gen_random_uuid(), 'Maria', 'INDIVIDUAL', '52998224725', 'm@ex.com', 'system', 'system');
                    """);

            try {
                runSql(connection, """
                        INSERT INTO customers (id, name, type, document, email, created_by, updated_by)
                        VALUES (gen_random_uuid(), 'Duplicada', 'INDIVIDUAL', '52998224725', 'd@ex.com', 'system', 'system');
                        """);
                throw new AssertionError("Expected unique constraint violation");
            } catch (Exception ex) {
                assertThat(ex.getMessage()).contains("uk_customers_document");
            }

            try (ResultSet resultSet = connection.getMetaData().getTables(null, null, "customers", null)) {
                assertThat(resultSet.next()).isTrue();
            }
        }
    }

    private void runSql(Connection connection, String sql) throws Exception {
        try (var statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
