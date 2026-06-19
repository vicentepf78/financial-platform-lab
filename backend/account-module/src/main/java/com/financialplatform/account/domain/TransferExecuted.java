package com.financialplatform.account.domain;

import com.financialplatform.sharedkernel.domain.DomainEvent;
import com.financialplatform.sharedkernel.domain.Identifier;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record TransferExecuted(
        UUID eventId,
        Identifier aggregateId,
        Identifier originAccountId,
        Identifier destinationAccountId,
        BigDecimal amount,
        String currency,
        String correlationId,
        Instant occurredAt) implements DomainEvent {

    public TransferExecuted {
        Objects.requireNonNull(eventId, "EventId is required");
        Objects.requireNonNull(aggregateId, "AggregateId is required");
        Objects.requireNonNull(originAccountId, "OriginAccountId is required");
        Objects.requireNonNull(destinationAccountId, "DestinationAccountId is required");
        Objects.requireNonNull(amount, "Amount is required");
        Objects.requireNonNull(currency, "Currency is required");
        Objects.requireNonNull(correlationId, "CorrelationId is required");
        Objects.requireNonNull(occurredAt, "OccurredAt is required");
    }

    @Override
    public String eventType() {
        return "TransferExecuted";
    }
}
