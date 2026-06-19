package com.financialplatform.account.support;

import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Shared PostgreSQL container for account-module integration tests.
 */
public final class AccountPostgresTestContainer {

    private static final PostgreSQLContainer<?> INSTANCE;

    static {
        INSTANCE = new PostgreSQLContainer<>("postgres:16-alpine")
                .withDatabaseName("financial_platform")
                .withUsername("financial")
                .withPassword("financial");
        INSTANCE.start();
    }

    private AccountPostgresTestContainer() {
    }

    public static PostgreSQLContainer<?> get() {
        return INSTANCE;
    }
}
