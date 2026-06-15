package com.financialplatform.account.features.createaccount;

import com.financialplatform.account.domain.Account;
import com.financialplatform.account.domain.AccountStatus;

import java.time.Instant;
import java.util.UUID;

public record CreateAccountResult(
        UUID id,
        UUID customerId,
        AccountStatus status,
        Instant createdAt) {

    public static CreateAccountResult from(Account account) {
        return new CreateAccountResult(
                account.id().value(),
                account.customerId().value(),
                account.status(),
                account.createdAt());
    }
}
