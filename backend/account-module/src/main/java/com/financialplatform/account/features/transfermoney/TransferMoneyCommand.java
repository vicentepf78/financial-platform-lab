package com.financialplatform.account.features.transfermoney;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferMoneyCommand(
        UUID originAccountId,
        UUID destinationAccountId,
        BigDecimal amount,
        UUID correlationId,
        String idempotencyKey,
        String actor) {
}
