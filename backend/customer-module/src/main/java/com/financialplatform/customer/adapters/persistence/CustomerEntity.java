package com.financialplatform.customer.adapters.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customers")
class CustomerEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(nullable = false, length = 14, unique = true)
    private String document;

    @Column(nullable = false)
    private String email;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "updated_by", nullable = false, length = 100)
    private String updatedBy;

    protected CustomerEntity() {
    }

    CustomerEntity(
            UUID id,
            String name,
            String type,
            String document,
            String email,
            Instant createdAt,
            String createdBy,
            Instant updatedAt,
            String updatedBy) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.document = document;
        this.email = email;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    UUID getId() {
        return id;
    }

    String getName() {
        return name;
    }

    String getType() {
        return type;
    }

    String getDocument() {
        return document;
    }

    String getEmail() {
        return email;
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
}
