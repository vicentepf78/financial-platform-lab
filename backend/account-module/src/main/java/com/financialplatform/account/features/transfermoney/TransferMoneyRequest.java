package com.financialplatform.account.features.transfermoney;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferMoneyRequest(
        @NotNull(message = "OriginAccountId is required")
        UUID originAccountId,
        @NotNull(message = "DestinationAccountId is required")
        UUID destinationAccountId,
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        @Digits(integer = 17, fraction = 2, message = "Amount must have at most 2 decimal places")
        BigDecimal amount,
        UUID correlationId,
        @Size(max = 100, message = "IdempotencyKey must be at most 100 characters")
        String idempotencyKey) {
}
