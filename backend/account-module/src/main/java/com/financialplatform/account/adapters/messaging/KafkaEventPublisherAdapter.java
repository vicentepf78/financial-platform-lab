package com.financialplatform.account.adapters.messaging;

import com.financialplatform.account.domain.AccountCreated;
import com.financialplatform.account.ports.EventPublisherPort;
import com.financialplatform.sharedkernel.domain.DomainEvent;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Objects;

public final class KafkaEventPublisherAdapter implements EventPublisherPort {

    static final String ACCOUNT_CREATED_TOPIC = "account-created";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AccountCreatedJsonSerializer serializer;

    public KafkaEventPublisherAdapter(
            KafkaTemplate<String, String> kafkaTemplate,
            AccountCreatedJsonSerializer serializer) {
        this.kafkaTemplate = Objects.requireNonNull(kafkaTemplate, "KafkaTemplate is required");
        this.serializer = Objects.requireNonNull(serializer, "Serializer is required");
    }

    @Override
    public void publish(DomainEvent event) {
        Objects.requireNonNull(event, "Event is required");
        if (!(event instanceof AccountCreated accountCreated)) {
            throw new IllegalArgumentException("Unsupported event type: " + event.eventType());
        }
        String key = accountCreated.aggregateId().value().toString();
        String payload = serializer.serialize(accountCreated);
        kafkaTemplate.send(ACCOUNT_CREATED_TOPIC, key, payload);
    }
}
