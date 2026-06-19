package com.financialplatform.account.adapters.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries_stub")
class LedgerEntryStubEntity {

    @Id
    private UUID id;

    @Column(name = "transfer_id")
    private UUID transferId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "entry_type", nullable = false, length = 10)
    private String entryType;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "correlation_id", nullable = false)
    private UUID correlationId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected LedgerEntryStubEntity() {
    }

    LedgerEntryStubEntity(
            UUID id,
            UUID transferId,
            UUID accountId,
            String entryType,
            BigDecimal amount,
            String currency,
            UUID correlationId,
            Instant createdAt) {
        this.id = id;
        this.transferId = transferId;
        this.accountId = accountId;
        this.entryType = entryType;
        this.amount = amount;
        this.currency = currency;
        this.correlationId = correlationId;
        this.createdAt = createdAt;
    }
}
