package com.financialplatform.account.features.transfermoney;

import com.financialplatform.account.domain.Account;
import com.financialplatform.account.support.AbstractAccountWebIntegrationTest;
import com.financialplatform.account.support.LedgerTestSupport;
import com.financialplatform.sharedkernel.domain.Identifier;
import com.financialplatform.sharedkernel.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static com.financialplatform.account.support.JwtTestSupport.bearerToken;
import static com.financialplatform.account.support.JwtTestSupport.generateToken;
import static com.financialplatform.account.support.JwtTestSupport.obtainOperatorToken;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class TransferMoneyControllerIntegrationTest extends AbstractAccountWebIntegrationTest {

    private static final String AMOUNT = "100.00";

    @Autowired
    private MockMvc mockMvc;

    private String operatorToken;

    @BeforeEach
    void authenticate() throws Exception {
        operatorToken = obtainOperatorToken(mockMvc);
    }

    @Test
    void shouldReturn401WhenTransferCalledWithoutToken() throws Exception {
        UUID originId = UUID.randomUUID();
        UUID destinationId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferPayload(originId, destinationId, AMOUNT, null, null)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value("https://api.financial-platform.lab/problems/invalid-token"))
                .andExpect(jsonPath("$.title").value("Authentication required"));
    }

    @Test
    void shouldReturn201WhenTransferIsValid() throws Exception {
        Identifier customerId = seedCustomer();
        Account origin = seedAccount(customerId);
        Account destination = seedAccount(customerId);
        LedgerTestSupport.creditAccount(ledgerPort, origin.id(), Money.brl("500.00"));
        UUID correlationId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/transfers")
                        .with(bearerToken(operatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferPayload(
                                origin.id().value(),
                                destination.id().value(),
                                AMOUNT,
                                correlationId,
                                null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.transferId").value(notNullValue()))
                .andExpect(jsonPath("$.data.originAccountId").value(origin.id().value().toString()))
                .andExpect(jsonPath("$.data.destinationAccountId").value(destination.id().value().toString()))
                .andExpect(jsonPath("$.data.amount").value(100.00))
                .andExpect(jsonPath("$.data.currency").value("BRL"))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.correlationId").value(correlationId.toString()))
                .andExpect(jsonPath("$.data.createdAt").value(notNullValue()))
                .andExpect(jsonPath("$.metadata").exists());

        String createdBy = jdbcTemplate.queryForObject(
                "SELECT created_by FROM transfers WHERE origin_account_id = ?",
                String.class,
                origin.id().value());
        assertThat(createdBy).isEqualTo("operator");
    }

    @Test
    void shouldReturn403WhenAuthenticatedUserDoesNotHaveTransferRole() throws Exception {
        Identifier customerId = seedCustomer();
        Account origin = seedAccount(customerId);
        Account destination = seedAccount(customerId);
        LedgerTestSupport.creditAccount(ledgerPort, origin.id(), Money.brl("500.00"));
        String viewerToken = generateToken("viewer", List.of("VIEWER"));

        mockMvc.perform(post("/api/v1/transfers")
                        .with(bearerToken(viewerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferPayload(
                                origin.id().value(), destination.id().value(), AMOUNT, null, null)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.type").value("https://api.financial-platform.lab/problems/access-denied"))
                .andExpect(jsonPath("$.detail")
                        .value("You do not have permission to access this resource"));
    }

    @Test
    void shouldReturn404WhenOriginAccountDoesNotExist() throws Exception {
        Identifier customerId = seedCustomer();
        Account destination = seedAccount(customerId);
        UUID unknownOriginId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/transfers")
                        .with(bearerToken(operatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferPayload(
                                unknownOriginId, destination.id().value(), AMOUNT, null, null)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("The requested account was not found."));
    }

    @Test
    void shouldReturn404WhenDestinationAccountDoesNotExist() throws Exception {
        Identifier customerId = seedCustomer();
        Account origin = seedAccount(customerId);
        UUID unknownDestinationId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/transfers")
                        .with(bearerToken(operatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferPayload(
                                origin.id().value(), unknownDestinationId, AMOUNT, null, null)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("The requested account was not found."));
    }

    @Test
    void shouldReturn400WhenOriginEqualsDestination() throws Exception {
        Identifier customerId = seedCustomer();
        Account account = seedAccount(customerId);
        UUID accountId = account.id().value();

        mockMvc.perform(post("/api/v1/transfers")
                        .with(bearerToken(operatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferPayload(accountId, accountId, AMOUNT, null, null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Origin and destination accounts must be different."));
    }

    @Test
    void shouldReturn409WhenOriginAccountIsClosed() throws Exception {
        Identifier customerId = seedCustomer();
        Account origin = seedClosedAccount(customerId);
        Account destination = seedAccount(customerId);
        LedgerTestSupport.creditAccount(ledgerPort, origin.id(), Money.brl("500.00"));

        mockMvc.perform(post("/api/v1/transfers")
                        .with(bearerToken(operatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferPayload(
                                origin.id().value(),
                                destination.id().value(),
                                AMOUNT,
                                null,
                                null)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("One or both accounts are not active."));
    }

    @Test
    void shouldReturn422WhenInsufficientBalance() throws Exception {
        Identifier customerId = seedCustomer();
        Account origin = seedAccount(customerId);
        Account destination = seedAccount(customerId);

        mockMvc.perform(post("/api/v1/transfers")
                        .with(bearerToken(operatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferPayload(
                                origin.id().value(),
                                destination.id().value(),
                                AMOUNT,
                                null,
                                null)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail")
                        .value("Origin account does not have sufficient balance for this transfer."));
    }

    @Test
    void shouldReturn400WhenAmountIsMissing() throws Exception {
        Identifier customerId = seedCustomer();
        Account origin = seedAccount(customerId);
        Account destination = seedAccount(customerId);

        mockMvc.perform(post("/api/v1/transfers")
                        .with(bearerToken(operatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "originAccountId": "%s",
                                  "destinationAccountId": "%s"
                                }
                                """
                                .formatted(origin.id().value(), destination.id().value())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("Amount")));
    }

    @Test
    void shouldReturn201WhenIdempotencyKeyAlreadyExists() throws Exception {
        Identifier customerId = seedCustomer();
        Account origin = seedAccount(customerId);
        Account destination = seedAccount(customerId);
        LedgerTestSupport.creditAccount(ledgerPort, origin.id(), Money.brl("500.00"));
        String idempotencyKey = "client-req-integration-001";

        String payload = transferPayload(
                origin.id().value(), destination.id().value(), AMOUNT, null, idempotencyKey);

        mockMvc.perform(post("/api/v1/transfers")
                        .with(bearerToken(operatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/transfers")
                        .with(bearerToken(operatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.originAccountId").value(origin.id().value().toString()))
                .andExpect(jsonPath("$.data.destinationAccountId").value(destination.id().value().toString()))
                .andExpect(jsonPath("$.data.amount").value(100.00));
    }

    @Test
    void shouldReturn409WhenIdempotencyKeyIsReusedWithDifferentPayload() throws Exception {
        Identifier customerId = seedCustomer();
        Account origin = seedAccount(customerId);
        Account destination = seedAccount(customerId);
        LedgerTestSupport.creditAccount(ledgerPort, origin.id(), Money.brl("500.00"));
        String idempotencyKey = "client-req-integration-conflict";

        mockMvc.perform(post("/api/v1/transfers")
                        .with(bearerToken(operatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferPayload(
                                origin.id().value(), destination.id().value(), AMOUNT, null, idempotencyKey)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/transfers")
                        .with(bearerToken(operatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferPayload(
                                origin.id().value(), destination.id().value(), "101.00", null, idempotencyKey)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail")
                        .value("This idempotency key was already used with a different request."));
    }

    private static String transferPayload(
            UUID originAccountId,
            UUID destinationAccountId,
            String amount,
            UUID correlationId,
            String idempotencyKey) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"originAccountId\": \"").append(originAccountId).append("\",");
        json.append("\"destinationAccountId\": \"").append(destinationAccountId).append("\",");
        if (correlationId != null) {
            json.append("\"correlationId\": \"").append(correlationId).append("\",");
        }
        if (idempotencyKey != null) {
            json.append("\"idempotencyKey\": \"").append(idempotencyKey).append("\",");
        }
        json.append("\"amount\": ").append(amount);
        json.append("}");
        return json.toString();
    }
}
