package com.financialplatform.account.adapters.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.financialplatform.account.domain.TransferExecuted;
import com.financialplatform.sharedkernel.domain.Identifier;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public final class TransferExecutedJsonSerializer {

    private final ObjectMapper objectMapper;

    public TransferExecutedJsonSerializer() {
        this(createObjectMapper());
    }

    TransferExecutedJsonSerializer(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "ObjectMapper is required");
    }

    public String serialize(TransferExecuted event) {
        Objects.requireNonNull(event, "Event is required");
        try {
            return objectMapper.writeValueAsString(TransferExecutedMessage.from(event));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize TransferExecuted event", exception);
        }
    }

    public TransferExecuted deserialize(String json) {
        Objects.requireNonNull(json, "Json is required");
        try {
            TransferExecutedMessage message = objectMapper.readValue(json, TransferExecutedMessage.class);
            return message.toDomainEvent();
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize TransferExecuted event", exception);
        }
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    record TransferExecutedMessage(
            UUID eventId,
            String eventType,
            UUID aggregateId,
            UUID originAccountId,
            UUID destinationAccountId,
            BigDecimal amount,
            String currency,
            String correlationId,
            java.time.Instant occurredAt) {

        static TransferExecutedMessage from(TransferExecuted event) {
            return new TransferExecutedMessage(
                    event.eventId(),
                    event.eventType(),
                    event.aggregateId().value(),
                    event.originAccountId().value(),
                    event.destinationAccountId().value(),
                    event.amount(),
                    event.currency(),
                    event.correlationId(),
                    event.occurredAt());
        }

        TransferExecuted toDomainEvent() {
            return new TransferExecuted(
                    eventId,
                    Identifier.of(aggregateId),
                    Identifier.of(originAccountId),
                    Identifier.of(destinationAccountId),
                    amount,
                    currency,
                    correlationId,
                    occurredAt);
        }
    }
}
