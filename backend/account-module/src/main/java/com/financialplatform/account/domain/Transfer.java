package com.financialplatform.account.domain;

import com.financialplatform.sharedkernel.domain.AggregateRoot;
import com.financialplatform.sharedkernel.domain.Identifier;
import com.financialplatform.sharedkernel.domain.Money;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Transfer extends AggregateRoot {

    private final Identifier id;
    private final Identifier originAccountId;
    private final Identifier destinationAccountId;
    private final Money amount;
    private final TransferStatus status;
    private final String correlationId;
    private final String idempotencyKey;
    private final String createdBy;
    private final Instant createdAt;

    private Transfer(
            Identifier id,
            Identifier originAccountId,
            Identifier destinationAccountId,
            Money amount,
            TransferStatus status,
            String correlationId,
            String idempotencyKey,
            String createdBy,
            Instant createdAt) {
        this.id = Objects.requireNonNull(id, "Id is required");
        this.originAccountId = Objects.requireNonNull(originAccountId, "OriginAccountId is required");
        this.destinationAccountId = Objects.requireNonNull(destinationAccountId, "DestinationAccountId is required");
        this.amount = Objects.requireNonNull(amount, "Amount is required");
        this.status = Objects.requireNonNull(status, "Status is required");
        this.correlationId = Objects.requireNonNull(correlationId, "CorrelationId is required");
        this.idempotencyKey = idempotencyKey;
        this.createdBy = createdBy;
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt is required");
    }

    public static Transfer execute(
            Identifier originAccountId,
            Identifier destinationAccountId,
            Money amount,
            String correlationId,
            Instant createdAt) {
        Objects.requireNonNull(originAccountId, "OriginAccountId is required");
        Objects.requireNonNull(destinationAccountId, "DestinationAccountId is required");
        Objects.requireNonNull(amount, "Amount is required");
        Objects.requireNonNull(correlationId, "CorrelationId is required");
        Objects.requireNonNull(createdAt, "CreatedAt is required");
        if (correlationId.isBlank()) {
            throw new IllegalArgumentException("CorrelationId must not be blank");
        }
        if (amount.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException(amount);
        }

        Identifier transferId = Identifier.generate();
        Transfer transfer = new Transfer(
                transferId,
                originAccountId,
                destinationAccountId,
                amount,
                TransferStatus.COMPLETED,
                correlationId,
                null,
                null,
                createdAt);
        transfer.registerEvent(new TransferExecuted(
                UUID.randomUUID(),
                transferId,
                originAccountId,
                destinationAccountId,
                amount.amount(),
                amount.currency().getCurrencyCode(),
                correlationId,
                createdAt));
        return transfer;
    }

    public static Transfer reconstitute(
            Identifier id,
            Identifier originAccountId,
            Identifier destinationAccountId,
            Money amount,
            TransferStatus status,
            String correlationId,
            Instant createdAt) {
        return reconstitute(
                id, originAccountId, destinationAccountId, amount, status, correlationId, null, null, createdAt);
    }

    public static Transfer reconstitute(
            Identifier id,
            Identifier originAccountId,
            Identifier destinationAccountId,
            Money amount,
            TransferStatus status,
            String correlationId,
            String idempotencyKey,
            String createdBy,
            Instant createdAt) {
        return new Transfer(
                id,
                originAccountId,
                destinationAccountId,
                amount,
                status,
                correlationId,
                idempotencyKey,
                createdBy,
                createdAt);
    }

    public Identifier id() {
        return id;
    }

    public Identifier originAccountId() {
        return originAccountId;
    }

    public Identifier destinationAccountId() {
        return destinationAccountId;
    }

    public Money amount() {
        return amount;
    }

    public TransferStatus status() {
        return status;
    }

    public String correlationId() {
        return correlationId;
    }

    public String idempotencyKey() {
        return idempotencyKey;
    }

    public String createdBy() {
        return createdBy;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
