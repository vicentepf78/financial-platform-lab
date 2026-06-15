package com.financialplatform.account.domain;

import com.financialplatform.sharedkernel.domain.DomainEvent;
import com.financialplatform.sharedkernel.domain.Identifier;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record AccountCreated(
        UUID eventId,
        Identifier aggregateId,
        Identifier customerId,
        AccountStatus status,
        Instant occurredAt,
        String createdBy) implements DomainEvent {

    public AccountCreated {
        Objects.requireNonNull(eventId, "EventId is required");
        Objects.requireNonNull(aggregateId, "AggregateId is required");
        Objects.requireNonNull(customerId, "CustomerId is required");
        Objects.requireNonNull(status, "Status is required");
        Objects.requireNonNull(occurredAt, "OccurredAt is required");
        Objects.requireNonNull(createdBy, "CreatedBy is required");
    }

    @Override
    public String eventType() {
        return "AccountCreated";
    }
}
