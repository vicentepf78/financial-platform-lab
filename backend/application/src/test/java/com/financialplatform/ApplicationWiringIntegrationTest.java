package com.financialplatform;

import com.financialplatform.account.adapters.ledger.LedgerStubAdapter;
import com.financialplatform.account.features.createaccount.CreateAccountController;
import com.financialplatform.account.features.createaccount.CreateAccountUseCase;
import com.financialplatform.account.features.transfermoney.TransferMoneyController;
import com.financialplatform.account.features.transfermoney.TransferMoneyUseCase;
import com.financialplatform.account.infrastructure.TransferMoneyTransactionalBoundary;
import com.financialplatform.account.domain.Account;
import com.financialplatform.account.ports.AccountRepositoryPort;
import com.financialplatform.account.ports.LedgerPort;
import com.financialplatform.sharedkernel.domain.Identifier;
import com.financialplatform.sharedkernel.domain.Money;
import com.financialplatform.customer.features.querycustomers.GetCustomerByIdUseCase;
import com.financialplatform.customer.features.querycustomers.QueryCustomersController;
import com.financialplatform.customer.features.querycustomers.QueryCustomersUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static com.financialplatform.support.JwtTestSupport.bearerToken;
import static com.financialplatform.support.JwtTestSupport.obtainOperatorToken;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = FinancialPlatformApplication.class)
@AutoConfigureMockMvc
@Testcontainers
class ApplicationWiringIntegrationTest {

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
    private CreateAccountUseCase createAccountUseCase;

    @Autowired
    private CreateAccountController createAccountController;

    @Autowired
    private QueryCustomersUseCase queryCustomersUseCase;

    @Autowired
    private GetCustomerByIdUseCase getCustomerByIdUseCase;

    @Autowired
    private QueryCustomersController queryCustomersController;

    @Autowired
    private TransferMoneyUseCase transferMoneyUseCase;

    @Autowired
    private TransferMoneyTransactionalBoundary transferMoneyTransactionalBoundary;

    @Autowired
    private TransferMoneyController transferMoneyController;

    @Autowired
    private AccountRepositoryPort accountRepository;

    @Autowired
    private LedgerPort ledgerPort;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
        jdbcTemplate.execute("DELETE FROM transfers");
        jdbcTemplate.execute("DELETE FROM accounts");
        jdbcTemplate.execute("DELETE FROM customers");
    }

    @Test
    void shouldLoadTransferMoneyBeans() {
        assertThat(transferMoneyUseCase).isNotNull();
        assertThat(transferMoneyTransactionalBoundary).isNotNull();
        assertThat(transferMoneyController).isNotNull();
    }

    @Test
    void shouldLoadAccountModuleBeans() {
        assertThat(createAccountUseCase).isNotNull();
        assertThat(createAccountController).isNotNull();
    }

    @Test
    void shouldLoadCustomerQueryBeans() {
        assertThat(queryCustomersUseCase).isNotNull();
        assertThat(getCustomerByIdUseCase).isNotNull();
        assertThat(queryCustomersController).isNotNull();
    }

    @Test
    void shouldRegisterQueryCustomersEndpoints() throws Exception {
        UUID customerId = seedCustomer();
        String token = obtainOperatorToken(mockMvc);

        mockMvc.perform(get("/api/v1/customers").with(bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(customerId.toString()))
                .andExpect(jsonPath("$.metadata.page").value(0))
                .andExpect(jsonPath("$.metadata.totalElements").value(1));

        mockMvc.perform(get("/api/v1/customers/{id}", customerId).with(bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(customerId.toString()))
                .andExpect(jsonPath("$.data.name").value("Maria Silva"))
                .andExpect(jsonPath("$.metadata").exists());
    }

    @Test
    void shouldRegisterCreateAccountEndpoint() throws Exception {
        UUID customerId = seedCustomer();
        String token = obtainOperatorToken(mockMvc);

        mockMvc.perform(post("/api/v1/accounts")
                        .with(bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": "%s"
                                }
                                """.formatted(customerId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(notNullValue()))
                .andExpect(jsonPath("$.data.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.metadata").exists());
    }

    @Test
    void shouldRegisterTransferMoneyEndpoint() throws Exception {
        UUID customerId = seedCustomer();
        String token = obtainOperatorToken(mockMvc);
        Instant now = Instant.parse("2026-06-15T10:00:00Z");

        Account origin = accountRepository.save(
                com.financialplatform.account.domain.Account.open(Identifier.of(customerId), "system", now));
        Account destination = accountRepository.save(
                com.financialplatform.account.domain.Account.open(Identifier.of(customerId), "system", now));
        ledgerPort.initializeAccount(origin.id());
        ledgerPort.initializeAccount(destination.id());
        requireLedgerStub(ledgerPort).creditAccount(origin.id(), Money.brl("500.00"));

        mockMvc.perform(post("/api/v1/transfers")
                        .with(bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "originAccountId": "%s",
                                  "destinationAccountId": "%s",
                                  "amount": 100.00
                                }
                                """
                                .formatted(origin.id().value(), destination.id().value())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.transferId").value(notNullValue()))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.metadata").exists());
    }

    @Test
    void shouldExecuteAuthenticatedFlowFromLoginThroughCustomersToAccountCreation() throws Exception {
        UUID customerId = seedCustomer();

        String token = obtainOperatorToken(mockMvc);

        mockMvc.perform(get("/api/v1/customers").with(bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(customerId.toString()));

        mockMvc.perform(post("/api/v1/accounts")
                        .with(bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": "%s"
                                }
                                """.formatted(customerId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(notNullValue()))
                .andExpect(jsonPath("$.data.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    private UUID seedCustomer() {
        UUID customerId = UUID.randomUUID();
        Instant now = Instant.parse("2026-06-15T10:00:00Z");
        jdbcTemplate.update(
                """
                INSERT INTO customers (id, name, type, document, email, created_at, created_by, updated_at, updated_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                customerId,
                "Maria Silva",
                "INDIVIDUAL",
                "52998224725",
                "maria@example.com",
                Timestamp.from(now),
                "system",
                Timestamp.from(now),
                "system");
        return customerId;
    }

    private static LedgerStubAdapter requireLedgerStub(LedgerPort ledgerPort) {
        if (ledgerPort instanceof LedgerStubAdapter stub) {
            return stub;
        }
        throw new IllegalStateException("Expected LedgerStubAdapter in integration test context");
    }
}
