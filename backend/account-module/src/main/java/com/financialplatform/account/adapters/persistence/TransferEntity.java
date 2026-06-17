package com.financialplatform.account.adapters.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transfers")
class TransferEntity {

    @Id
    private UUID id;

    @Column(name = "origin_account_id", nullable = false)
    private UUID originAccountId;

    @Column(name = "destination_account_id", nullable = false)
    private UUID destinationAccountId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "correlation_id", nullable = false)
    private UUID correlationId;

    @Column(name = "idempotency_key", length = 100)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    protected TransferEntity() {
    }

    TransferEntity(
            UUID id,
            UUID originAccountId,
            UUID destinationAccountId,
            BigDecimal amount,
            String currency,
            String status,
            UUID correlationId,
            String idempotencyKey,
            Instant createdAt,
            String createdBy) {
        this.id = id;
        this.originAccountId = originAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.correlationId = correlationId;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
    }

    UUID getId() {
        return id;
    }

    UUID getOriginAccountId() {
        return originAccountId;
    }

    UUID getDestinationAccountId() {
        return destinationAccountId;
    }

    BigDecimal getAmount() {
        return amount;
    }

    String getCurrency() {
        return currency;
    }

    String getStatus() {
        return status;
    }

    UUID getCorrelationId() {
        return correlationId;
    }

    String getIdempotencyKey() {
        return idempotencyKey;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    String getCreatedBy() {
        return createdBy;
    }
}
