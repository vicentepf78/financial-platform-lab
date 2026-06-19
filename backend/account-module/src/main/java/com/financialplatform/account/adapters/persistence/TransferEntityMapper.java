package com.financialplatform.account.adapters.persistence;

import com.financialplatform.account.domain.Transfer;
import com.financialplatform.account.domain.TransferStatus;
import com.financialplatform.sharedkernel.domain.Identifier;
import com.financialplatform.sharedkernel.domain.Money;

import java.util.UUID;

final class TransferEntityMapper {

    private static final String DEFAULT_CREATED_BY = "system";

    private TransferEntityMapper() {
    }

    static TransferEntity toEntity(Transfer transfer) {
        String createdBy = transfer.createdBy() != null ? transfer.createdBy() : DEFAULT_CREATED_BY;
        return new TransferEntity(
                transfer.id().value(),
                transfer.originAccountId().value(),
                transfer.destinationAccountId().value(),
                transfer.amount().amount(),
                transfer.amount().currency().getCurrencyCode(),
                transfer.status().name(),
                UUID.fromString(transfer.correlationId()),
                transfer.idempotencyKey(),
                transfer.createdAt(),
                createdBy);
    }

    static Transfer toDomain(TransferEntity entity) {
        return Transfer.reconstitute(
                Identifier.of(entity.getId()),
                Identifier.of(entity.getOriginAccountId()),
                Identifier.of(entity.getDestinationAccountId()),
                Money.brl(entity.getAmount()),
                TransferStatus.valueOf(entity.getStatus()),
                entity.getCorrelationId().toString(),
                entity.getIdempotencyKey(),
                entity.getCreatedBy(),
                entity.getCreatedAt());
    }
}
