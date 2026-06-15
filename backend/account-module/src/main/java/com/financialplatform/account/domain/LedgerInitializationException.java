package com.financialplatform.account.domain;

import java.util.UUID;

public class LedgerInitializationException extends RuntimeException {

    public LedgerInitializationException(UUID accountId, Throwable cause) {
        super("Failed to initialize ledger for account: " + accountId, cause);
    }
}
