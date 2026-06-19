package com.financialplatform.account.adapters.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts")
class AccountEntity {

    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "updated_by", nullable = false, length = 100)
    private String updatedBy;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    protected AccountEntity() {
    }

    AccountEntity(
            UUID id,
            UUID customerId,
            Instant createdAt,
            String createdBy,
            Instant updatedAt,
            String updatedBy,
            String status) {
        this.id = id;
        this.customerId = customerId;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
        this.status = status;
    }

    UUID getId() {
        return id;
    }

    UUID getCustomerId() {
        return customerId;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    String getCreatedBy() {
        return createdBy;
    }

    Instant getUpdatedAt() {
        return updatedAt;
    }

    String getUpdatedBy() {
        return updatedBy;
    }

    String getStatus() {
        return status;
    }
}
