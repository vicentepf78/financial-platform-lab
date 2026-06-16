package com.financialplatform.account.features.createaccount;

import com.financialplatform.account.support.AbstractAccountWebIntegrationTest;
import com.financialplatform.sharedkernel.domain.Identifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static com.financialplatform.account.support.JwtTestSupport.bearerToken;
import static com.financialplatform.account.support.JwtTestSupport.obtainOperatorToken;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class CreateAccountControllerIntegrationTest extends AbstractAccountWebIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String operatorToken;

    @BeforeEach
    void authenticate() throws Exception {
        operatorToken = obtainOperatorToken(mockMvc);
    }

    @Test
    void shouldReturn401WhenCreateAccountCalledWithoutToken() throws Exception {
        Identifier customerId = seedCustomer();

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": "%s"
                                }
                                """.formatted(customerId.value())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value("https://api.financial-platform.lab/problems/invalid-token"))
                .andExpect(jsonPath("$.title").value("Authentication required"));
    }

    @Test
    void shouldReturn201WhenCustomerExists() throws Exception {
        Identifier customerId = seedCustomer();

        mockMvc.perform(post("/api/v1/accounts")
                        .with(bearerToken(operatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": "%s"
                                }
                                """.formatted(customerId.value())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(notNullValue()))
                .andExpect(jsonPath("$.data.customerId").value(customerId.value().toString()))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.createdAt").value(notNullValue()))
                .andExpect(jsonPath("$.metadata").exists());
    }

    @Test
    void shouldReturn404WhenCustomerDoesNotExist() throws Exception {
        UUID unknownCustomerId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/accounts")
                        .with(bearerToken(operatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": "%s"
                                }
                                """.formatted(unknownCustomerId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value(containsString(unknownCustomerId.toString())));
    }

    @Test
    void shouldReturn400WhenCustomerIdIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/accounts")
                        .with(bearerToken(operatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void shouldReturn400WhenCustomerIdIsNull() throws Exception {
        mockMvc.perform(post("/api/v1/accounts")
                        .with(bearerToken(operatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());
    }
}
