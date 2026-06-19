package com.financialplatform.account.domain;

import com.financialplatform.sharedkernel.domain.Identifier;

public class SameAccountTransferException extends RuntimeException {

    public SameAccountTransferException(Identifier accountId) {
        super("Origin and destination must be different accounts: " + accountId);
    }
}
