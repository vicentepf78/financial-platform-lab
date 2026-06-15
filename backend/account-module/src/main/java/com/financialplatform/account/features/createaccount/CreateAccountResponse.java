package com.financialplatform.account.features.createaccount;

import com.financialplatform.account.domain.AccountStatus;

import java.time.Instant;
import java.util.UUID;

public record CreateAccountResponse(
        UUID id,
        UUID customerId,
        AccountStatus status,
        Instant createdAt) {

    public static CreateAccountResponse from(CreateAccountResult result) {
        return new CreateAccountResponse(
                result.id(),
                result.customerId(),
                result.status(),
                result.createdAt());
    }
}
