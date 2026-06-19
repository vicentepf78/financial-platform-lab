package com.financialplatform.account.domain;

import com.financialplatform.sharedkernel.domain.Identifier;

public class InactiveAccountException extends RuntimeException {

    public InactiveAccountException(Identifier accountId, AccountStatus status) {
        super("Account is not active: " + accountId + " (status=" + status + ")");
    }
}
