package com.financialplatform.features.transfermoney;

import com.financialplatform.FinancialPlatformApplication;
import com.financialplatform.account.adapters.messaging.KafkaEventPublisherAdapter;
import com.financialplatform.account.adapters.messaging.TransferExecutedJsonSerializer;
import com.financialplatform.account.domain.TransferExecuted;
import com.financialplatform.account.adapters.ledger.LedgerStubAdapter;
import com.financialplatform.account.ports.LedgerPort;
import com.financialplatform.sharedkernel.domain.Identifier;
import com.financialplatform.sharedkernel.domain.Money;
import com.jayway.jsonpath.JsonPath;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.financialplatform.support.JwtTestSupport.bearerToken;
import static com.financialplatform.support.JwtTestSupport.obtainOperatorToken;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sprint 1 integrated verify: customer → accounts → credit → transfer → balances.
 */
@SpringBootTest(classes = FinancialPlatformApplication.class)
@AutoConfigureMockMvc
@Testcontainers
class TransferMoneyIntegratedFlowIntegrationTest {

    private static final String VALID_CPF = "529.982.247-25";
    private static final String INITIAL_CREDIT = "500.00";
    private static final String TRANSFER_AMOUNT = "100.00";

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("financial_platform")
            .withUsername("financial")
            .withPassword("financial");

    @Container
    static final KafkaContainer KAFKA = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LedgerPort ledgerPort;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TransferExecutedJsonSerializer transferExecutedSerializer;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("security.jwt.enabled", () -> "true");
    }

    @BeforeEach
    void cleanData() {
        createTopic();
        jdbcTemplate.execute("DELETE FROM ledger_entries_stub");
        jdbcTemplate.execute("DELETE FROM transfers");
        jdbcTemplate.execute("DELETE FROM accounts");
        jdbcTemplate.execute("DELETE FROM customers");
    }

    @Test
    void shouldCompleteSprint1TransferFlowFromCustomerCreationThroughBalanceVerification() throws Exception {
        String token = obtainOperatorToken(mockMvc);

        UUID customerId = createCustomer(token);
        UUID originAccountId = createAccount(token, customerId);
        UUID destinationAccountId = createAccount(token, customerId);

        creditAccount(Identifier.of(originAccountId), Money.brl(INITIAL_CREDIT));

        try (Consumer<String, String> consumer = createConsumer()) {
            consumer.subscribe(List.of(KafkaEventPublisherAdapter.TRANSFER_EXECUTED_TOPIC));

            MvcResult transferResult = mockMvc.perform(post("/api/v1/transfers")
                            .with(bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "originAccountId": "%s",
                                      "destinationAccountId": "%s",
                                      "amount": %s
                                    }
                                    """
                                    .formatted(originAccountId, destinationAccountId, TRANSFER_AMOUNT)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.data.amount").value(100.00))
                    .andReturn();

            String transferId = JsonPath.read(transferResult.getResponse().getContentAsString(), "$.data.transferId");
            ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(10));
            assertThat(records.count()).isEqualTo(1);

            ConsumerRecord<String, String> record = records.iterator().next();
            TransferExecuted event = transferExecutedSerializer.deserialize(record.value());
            assertThat(record.key()).isEqualTo(transferId);
            assertThat(event.aggregateId().value().toString()).isEqualTo(transferId);
            assertThat(event.originAccountId().value()).isEqualTo(originAccountId);
            assertThat(event.destinationAccountId().value()).isEqualTo(destinationAccountId);
            assertThat(event.amount()).isEqualByComparingTo(TRANSFER_AMOUNT);
        }

        assertThat(ledgerPort.getBalanceProjection(Identifier.of(originAccountId)))
                .isEqualTo(Money.brl("400.00"));
        assertThat(ledgerPort.getBalanceProjection(Identifier.of(destinationAccountId)))
                .isEqualTo(Money.brl(TRANSFER_AMOUNT));
    }

    private UUID createCustomer(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/customers")
                        .with(bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Maria Silva",
                                  "type": "INDIVIDUAL",
                                  "document": "%s",
                                  "email": "maria@example.com"
                                }
                                """.formatted(VALID_CPF)))
                .andExpect(status().isCreated())
                .andReturn();

        return UUID.fromString(JsonPath.read(result.getResponse().getContentAsString(), "$.data.id"));
    }

    private UUID createAccount(String token, UUID customerId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/accounts")
                        .with(bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": "%s"
                                }
                                """.formatted(customerId)))
                .andExpect(status().isCreated())
                .andReturn();

        return UUID.fromString(JsonPath.read(result.getResponse().getContentAsString(), "$.data.id"));
    }

    private void creditAccount(Identifier accountId, Money amount) {
        requireLedgerStub(ledgerPort).creditAccount(accountId, amount);
    }

    private static void createTopic() {
        try (var adminClient = org.apache.kafka.clients.admin.AdminClient.create(
                Map.of("bootstrap.servers", KAFKA.getBootstrapServers()))) {
            adminClient.createTopics(List.of(
                    new org.apache.kafka.clients.admin.NewTopic(
                            KafkaEventPublisherAdapter.TRANSFER_EXECUTED_TOPIC, 1, (short) 1)));
        }
    }

    private static Consumer<String, String> createConsumer() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                KAFKA.getBootstrapServers(),
                "transfer-money-integrated-flow-test",
                "true");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new KafkaConsumer<>(consumerProps);
    }

    private static LedgerStubAdapter requireLedgerStub(LedgerPort ledgerPort) {
        if (ledgerPort instanceof LedgerStubAdapter stub) {
            return stub;
        }
        throw new IllegalStateException("Expected LedgerStubAdapter in integration test context");
    }
}
