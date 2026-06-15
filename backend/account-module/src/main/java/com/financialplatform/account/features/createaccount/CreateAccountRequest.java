package com.financialplatform.account.features.createaccount;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateAccountRequest(
        @NotNull(message = "CustomerId is required")
        UUID customerId) {
}
