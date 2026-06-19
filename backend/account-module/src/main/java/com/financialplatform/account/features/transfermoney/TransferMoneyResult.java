package com.financialplatform.account.features.transfermoney;

import com.financialplatform.account.domain.Transfer;
import com.financialplatform.account.domain.TransferStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferMoneyResult(
        UUID transferId,
        UUID originAccountId,
        UUID destinationAccountId,
        BigDecimal amount,
        String currency,
        TransferStatus status,
        String correlationId,
        Instant createdAt) {

    public static TransferMoneyResult from(Transfer transfer) {
        return new TransferMoneyResult(
                transfer.id().value(),
                transfer.originAccountId().value(),
                transfer.destinationAccountId().value(),
                transfer.amount().amount(),
                transfer.amount().currency().getCurrencyCode(),
                transfer.status(),
                transfer.correlationId(),
                transfer.createdAt());
    }
}
