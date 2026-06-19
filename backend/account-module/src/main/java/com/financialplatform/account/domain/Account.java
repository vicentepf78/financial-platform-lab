package com.financialplatform.account.domain;

import com.financialplatform.sharedkernel.domain.AggregateRoot;
import com.financialplatform.sharedkernel.domain.Identifier;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Account extends AggregateRoot {

    private final Identifier id;
    private final Identifier customerId;
    private final AccountStatus status;
    private final Instant createdAt;
    private final String createdBy;

    private Account(
            Identifier id,
            Identifier customerId,
            AccountStatus status,
            Instant createdAt,
            String createdBy) {
        this.id = Objects.requireNonNull(id, "Id is required");
        this.customerId = Objects.requireNonNull(customerId, "CustomerId is required");
        this.status = Objects.requireNonNull(status, "Status is required");
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt is required");
        this.createdBy = Objects.requireNonNull(createdBy, "CreatedBy is required");
    }

    public static Account reconstitute(
            Identifier id,
            Identifier customerId,
            AccountStatus status,
            Instant createdAt,
            String createdBy) {
        return new Account(id, customerId, status, createdAt, createdBy);
    }

    public static Account open(Identifier customerId, String actor, Instant now) {
        Objects.requireNonNull(customerId, "CustomerId is required");
        Objects.requireNonNull(actor, "Actor is required");
        Objects.requireNonNull(now, "Timestamp is required");
        if (actor.isBlank()) {
            throw new IllegalArgumentException("Actor must not be blank");
        }

        Identifier id = Identifier.generate();
        Account account = new Account(id, customerId, AccountStatus.ACTIVE, now, actor);
        account.registerEvent(new AccountCreated(
                UUID.randomUUID(),
                id,
                customerId,
                AccountStatus.ACTIVE,
                now,
                actor));
        return account;
    }

    public Identifier id() {
        return id;
    }

    public Identifier customerId() {
        return customerId;
    }

    public AccountStatus status() {
        return status;
    }

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public String createdBy() {
        return createdBy;
    }
}
