package com.financialplatform.account.adapters.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.financialplatform.account.domain.AccountCreated;

import java.util.Objects;
import java.util.UUID;

public final class AccountCreatedJsonSerializer {

    private final ObjectMapper objectMapper;

    public AccountCreatedJsonSerializer() {
        this(createObjectMapper());
    }

    AccountCreatedJsonSerializer(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "ObjectMapper is required");
    }

    public String serialize(AccountCreated event) {
        Objects.requireNonNull(event, "Event is required");
        try {
            return objectMapper.writeValueAsString(AccountCreatedMessage.from(event));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize AccountCreated event", exception);
        }
    }

    public AccountCreated deserialize(String json) {
        Objects.requireNonNull(json, "Json is required");
        try {
            AccountCreatedMessage message = objectMapper.readValue(json, AccountCreatedMessage.class);
            return message.toDomainEvent();
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize AccountCreated event", exception);
        }
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    record AccountCreatedMessage(
            UUID eventId,
            String eventType,
            UUID aggregateId,
            UUID customerId,
            String status,
            java.time.Instant occurredAt,
            String createdBy) {

        static AccountCreatedMessage from(AccountCreated event) {
            return new AccountCreatedMessage(
                    event.eventId(),
                    event.eventType(),
                    event.aggregateId().value(),
                    event.customerId().value(),
                    event.status().name(),
                    event.occurredAt(),
                    event.createdBy());
        }

        AccountCreated toDomainEvent() {
            return new AccountCreated(
                    eventId,
                    com.financialplatform.sharedkernel.domain.Identifier.of(aggregateId),
                    com.financialplatform.sharedkernel.domain.Identifier.of(customerId),
                    com.financialplatform.account.domain.AccountStatus.valueOf(status),
                    occurredAt,
                    createdBy);
        }
    }
}
