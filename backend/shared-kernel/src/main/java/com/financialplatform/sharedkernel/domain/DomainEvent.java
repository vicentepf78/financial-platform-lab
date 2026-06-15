package com.financialplatform.sharedkernel.domain;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {

    UUID eventId();

    String eventType();

    Instant occurredAt();

    Identifier aggregateId();
}
