package com.financialplatform.account.adapters.messaging;

import com.financialplatform.account.domain.AccountCreated;
import com.financialplatform.account.domain.TransferExecuted;
import com.financialplatform.account.ports.EventPublisherPort;
import com.financialplatform.sharedkernel.domain.DomainEvent;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Objects;

public final class KafkaEventPublisherAdapter implements EventPublisherPort {

    static final String ACCOUNT_CREATED_TOPIC = "account-created";
    static final String TRANSFER_EXECUTED_TOPIC = "transfer-executed";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AccountCreatedJsonSerializer accountCreatedSerializer;
    private final TransferExecutedJsonSerializer transferExecutedSerializer;

    public KafkaEventPublisherAdapter(
            KafkaTemplate<String, String> kafkaTemplate,
            AccountCreatedJsonSerializer accountCreatedSerializer,
            TransferExecutedJsonSerializer transferExecutedSerializer) {
        this.kafkaTemplate = Objects.requireNonNull(kafkaTemplate, "KafkaTemplate is required");
        this.accountCreatedSerializer = Objects.requireNonNull(accountCreatedSerializer, "AccountCreated serializer is required");
        this.transferExecutedSerializer = Objects.requireNonNull(transferExecutedSerializer, "TransferExecuted serializer is required");
    }

    @Override
    public void publish(DomainEvent event) {
        Objects.requireNonNull(event, "Event is required");
        if (event instanceof AccountCreated accountCreated) {
            publishAccountCreated(accountCreated);
        } else if (event instanceof TransferExecuted transferExecuted) {
            publishTransferExecuted(transferExecuted);
        } else {
            throw new IllegalArgumentException("Unsupported event type: " + event.eventType());
        }
    }

    private void publishAccountCreated(AccountCreated accountCreated) {
        String key = accountCreated.aggregateId().value().toString();
        String payload = accountCreatedSerializer.serialize(accountCreated);
        kafkaTemplate.send(ACCOUNT_CREATED_TOPIC, key, payload);
    }

    private void publishTransferExecuted(TransferExecuted transferExecuted) {
        String key = transferExecuted.aggregateId().value().toString();
        String payload = transferExecutedSerializer.serialize(transferExecuted);
        kafkaTemplate.send(TRANSFER_EXECUTED_TOPIC, key, payload);
    }
}
