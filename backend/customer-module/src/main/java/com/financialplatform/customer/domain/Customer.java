package com.financialplatform.customer.domain;

import com.financialplatform.sharedkernel.domain.AggregateRoot;
import com.financialplatform.sharedkernel.domain.Identifier;

import java.time.Instant;
import java.util.Objects;

public final class Customer extends AggregateRoot {

    private final Identifier id;
    private final String name;
    private final CustomerType type;
    private final Document document;
    private final Email email;
    private final Instant createdAt;
    private final String createdBy;
    private final Instant updatedAt;
    private final String updatedBy;

    private Customer(
            Identifier id,
            String name,
            CustomerType type,
            Document document,
            Email email,
            Instant createdAt,
            String createdBy,
            Instant updatedAt,
            String updatedBy) {
        this.id = Objects.requireNonNull(id, "Id is required");
        this.name = Objects.requireNonNull(name, "Name is required");
        this.type = Objects.requireNonNull(type, "Type is required");
        this.document = Objects.requireNonNull(document, "Document is required");
        this.email = Objects.requireNonNull(email, "Email is required");
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt is required");
        this.createdBy = Objects.requireNonNull(createdBy, "CreatedBy is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "UpdatedAt is required");
        this.updatedBy = Objects.requireNonNull(updatedBy, "UpdatedBy is required");
    }

    public static Customer reconstitute(
            Identifier id,
            String name,
            CustomerType type,
            Document document,
            Email email,
            Instant createdAt,
            String createdBy,
            Instant updatedAt,
            String updatedBy) {
        return new Customer(id, name, type, document, email, createdAt, createdBy, updatedAt, updatedBy);
    }

    public static Customer create(
            String name,
            CustomerType type,
            String documentRaw,
            String emailRaw,
            String actor,
            Instant now) {
        Objects.requireNonNull(name, "Name is required");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }
        Objects.requireNonNull(actor, "Actor is required");
        Objects.requireNonNull(now, "Timestamp is required");

        Document document = Document.of(type, documentRaw);
        Email email = Email.of(emailRaw);

        return new Customer(
                Identifier.generate(),
                name.trim(),
                type,
                document,
                email,
                now,
                actor,
                now,
                actor);
    }

    public Identifier id() {
        return id;
    }

    public String name() {
        return name;
    }

    public CustomerType type() {
        return type;
    }

    public Document document() {
        return document;
    }

    public Email email() {
        return email;
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

    public Customer update(
            String name,
            String email,
            CustomerType type,
            String documentRaw,
            String actor,
            Instant now) {
        Objects.requireNonNull(actor, "Actor is required");
        Objects.requireNonNull(now, "Timestamp is required");

        if (type != null) {
            throw new ImmutableFieldException("type");
        }
        if (documentRaw != null) {
            throw new ImmutableFieldException("document");
        }

        String newName = this.name;
        Email newEmail = this.email;

        if (name != null) {
            if (name.isBlank()) {
                throw new IllegalArgumentException("Name must not be blank");
            }
            newName = name.trim();
        }

        if (email != null) {
            newEmail = Email.of(email);
        }

        return new Customer(
                id,
                newName,
                this.type,
                this.document,
                newEmail,
                createdAt,
                createdBy,
                now,
                actor);
    }
}
