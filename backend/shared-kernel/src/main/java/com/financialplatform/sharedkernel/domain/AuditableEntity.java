package com.financialplatform.sharedkernel.domain;

import java.time.Instant;
import java.util.Objects;

public abstract class AuditableEntity {

    private final Identifier id;
    private final Instant createdAt;
    private final String createdBy;
    private Instant updatedAt;
    private String updatedBy;

    protected AuditableEntity(Identifier id, Instant createdAt, String createdBy) {
        this.id = Objects.requireNonNull(id, "Id is required");
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt is required");
        this.createdBy = Objects.requireNonNull(createdBy, "CreatedBy is required");
        this.updatedAt = createdAt;
        this.updatedBy = createdBy;
    }

    public Identifier id() {
        return id;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public String createdBy() {
        return createdBy;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public String updatedBy() {
        return updatedBy;
    }

    protected void touch(String actor, Instant at) {
        this.updatedAt = Objects.requireNonNull(at, "UpdatedAt is required");
        this.updatedBy = Objects.requireNonNull(actor, "UpdatedBy is required");
    }
}
