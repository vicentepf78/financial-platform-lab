package com.financialplatform.account.adapters.messaging;

import com.financialplatform.account.domain.AccountCreated;
import com.financialplatform.account.domain.AccountStatus;
import com.financialplatform.account.ports.EventPublisherPort;
import com.financialplatform.account.support.AccountMessagingTestApplication;
import com.financialplatform.sharedkernel.domain.Identifier;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = AccountMessagingTestApplication.class)
@Testcontainers
class KafkaEventPublisherIntegrationTest {

    private static final Instant NOW = Instant.parse("2026-06-15T10:00:00Z");

    @Container
    static final KafkaContainer KAFKA = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @Autowired
    private EventPublisherPort eventPublisher;

    @Autowired
    private AccountCreatedJsonSerializer serializer;

    @DynamicPropertySource
    static void configureKafka(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
    }

    @BeforeEach
    void createTopic() {
        try (var adminClient = org.apache.kafka.clients.admin.AdminClient.create(
                Map.of("bootstrap.servers", KAFKA.getBootstrapServers()))) {
            adminClient.createTopics(List.of(
                    new org.apache.kafka.clients.admin.NewTopic(
                            KafkaEventPublisherAdapter.ACCOUNT_CREATED_TOPIC, 1, (short) 1)));
        }
    }

    @Test
    void shouldPublishAccountCreatedEventToAccountCreatedTopic() {
        UUID eventId = UUID.randomUUID();
        Identifier accountId = Identifier.of("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
        Identifier customerId = Identifier.of("b2c3d4e5-f6a7-8901-bcde-f12345678901");
        AccountCreated event = new AccountCreated(
                eventId,
                accountId,
                customerId,
                AccountStatus.ACTIVE,
                NOW,
                "system");

        try (Consumer<String, String> consumer = createConsumer()) {
            consumer.subscribe(List.of(KafkaEventPublisherAdapter.ACCOUNT_CREATED_TOPIC));

            eventPublisher.publish(event);

            ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(10));
            assertThat(records.count()).isEqualTo(1);

            ConsumerRecord<String, String> record = records.iterator().next();
            assertThat(record.key()).isEqualTo(accountId.value().toString());
            assertThat(record.topic()).isEqualTo(KafkaEventPublisherAdapter.ACCOUNT_CREATED_TOPIC);

            AccountCreated deserialized = serializer.deserialize(record.value());
            assertThat(deserialized.eventId()).isEqualTo(eventId);
            assertThat(deserialized.aggregateId()).isEqualTo(accountId);
            assertThat(deserialized.customerId()).isEqualTo(customerId);
            assertThat(deserialized.status()).isEqualTo(AccountStatus.ACTIVE);
            assertThat(deserialized.occurredAt()).isEqualTo(NOW);
            assertThat(deserialized.createdBy()).isEqualTo("system");
            assertThat(deserialized.eventType()).isEqualTo("AccountCreated");
        }
    }

    @Test
    void shouldSerializeAccountCreatedAsJsonWithExpectedFields() {
        UUID eventId = UUID.fromString("11111111-2222-3333-4444-555555555555");
        Identifier accountId = Identifier.of("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        Identifier customerId = Identifier.of("ffffffff-1111-2222-3333-444444444444");
        AccountCreated event = new AccountCreated(
                eventId,
                accountId,
                customerId,
                AccountStatus.ACTIVE,
                NOW,
                "operator");

        String json = serializer.serialize(event);

        assertThat(json)
                .contains("\"eventId\":\"11111111-2222-3333-4444-555555555555\"")
                .contains("\"eventType\":\"AccountCreated\"")
                .contains("\"aggregateId\":\"aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee\"")
                .contains("\"customerId\":\"ffffffff-1111-2222-3333-444444444444\"")
                .contains("\"status\":\"ACTIVE\"")
                .contains("\"occurredAt\":\"2026-06-15T10:00:00Z\"")
                .contains("\"createdBy\":\"operator\"");
    }

    private Consumer<String, String> createConsumer() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                KAFKA.getBootstrapServers(),
                "account-module-test",
                "true");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new KafkaConsumer<>(consumerProps);
    }
}
