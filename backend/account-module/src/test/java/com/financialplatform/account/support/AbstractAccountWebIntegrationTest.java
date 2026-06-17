package com.financialplatform.account.support;

import com.financialplatform.sharedkernel.domain.Identifier;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@SpringBootTest(classes = AccountModuleTestApplication.class)
public abstract class AbstractAccountWebIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        PostgreSQLContainer<?> postgres = AccountPostgresTestContainer.get();
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.locations", () -> "filesystem:src/test/resources/db/migration");
        registry.add("security.jwt.enabled", () -> "true");
        registry.add("security.jwt.secret", () -> "integration-test-jwt-secret-key-32chars");
        registry.add("security.jwt.expiration-seconds", () -> "3600");
    }

    @BeforeEach
    void cleanData() {
        jdbcTemplate.execute("DELETE FROM transfers");
        jdbcTemplate.execute("DELETE FROM accounts");
        jdbcTemplate.execute("DELETE FROM customers");
    }

    protected Identifier seedCustomer() {
        UUID customerId = UUID.randomUUID();
        Instant now = Instant.parse("2026-06-15T10:00:00Z");
        jdbcTemplate.update(
                """
                INSERT INTO customers (id, name, type, document, email, created_at, created_by, updated_at, updated_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                customerId,
                "Maria Silva",
                "INDIVIDUAL",
                "52998224725",
                "maria@example.com",
                Timestamp.from(now),
                "system",
                Timestamp.from(now),
                "system");
        return Identifier.of(customerId);
    }
}
