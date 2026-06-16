package com.financialplatform.customer.features.updatecustomer;

import com.financialplatform.customer.support.AbstractCustomerIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static com.financialplatform.customer.support.JwtTestSupport.bearerToken;
import static com.financialplatform.customer.support.JwtTestSupport.obtainOperatorToken;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class UpdateCustomerControllerIntegrationTest extends AbstractCustomerIntegrationTest {

    private static final String VALID_CPF = "529.982.247-25";

    @Autowired
    private MockMvc mockMvc;

    private String operatorToken;

    @BeforeEach
    void authenticate() throws Exception {
        operatorToken = obtainOperatorToken(mockMvc);
    }

    @Test
    void shouldReturn200WhenCustomerIsUpdatedSuccessfully() throws Exception {
        String customerId = createCustomer("Maria Silva", VALID_CPF, "maria@example.com");

        mockMvc.perform(patch("/api/v1/customers/{id}", customerId)
                        .with(bearerToken(operatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Maria Santos",
                                  "email": "maria.santos@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(customerId))
                .andExpect(jsonPath("$.data.name").value("Maria Santos"))
                .andExpect(jsonPath("$.data.email").value("maria.santos@example.com"))
                .andExpect(jsonPath("$.data.type").value("INDIVIDUAL"))
                .andExpect(jsonPath("$.data.document").value(VALID_CPF))
                .andExpect(jsonPath("$.data.createdAt").value(notNullValue()))
                .andExpect(jsonPath("$.data.updatedAt").value(notNullValue()))
                .andExpect(jsonPath("$.metadata").exists());
    }

    @Test
    void shouldReturn404WhenCustomerDoesNotExist() throws Exception {
        UUID missingId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        mockMvc.perform(patch("/api/v1/customers/{id}", missingId)
                        .with(bearerToken(operatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Maria Santos"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value(containsString(missingId.toString())));
    }

    @Test
    void shouldReturn400WhenDocumentIsSentInRequest() throws Exception {
        String customerId = createCustomer("Maria Silva", VALID_CPF, "maria@example.com");

        mockMvc.perform(patch("/api/v1/customers/{id}", customerId)
                        .with(bearerToken(operatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Maria Santos",
                                  "document": "%s"
                                }
                                """.formatted(VALID_CPF)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("document")));
    }

    @Test
    void shouldReturn400WhenTypeIsSentInRequest() throws Exception {
        String customerId = createCustomer("Maria Silva", VALID_CPF, "maria@example.com");

        mockMvc.perform(patch("/api/v1/customers/{id}", customerId)
                        .with(bearerToken(operatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Maria Santos",
                                  "type": "COMPANY"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("type")));
    }

    @Test
    void shouldReturn400WhenBodyIsEmpty() throws Exception {
        String customerId = createCustomer("Maria Silva", VALID_CPF, "maria@example.com");

        mockMvc.perform(patch("/api/v1/customers/{id}", customerId)
                        .with(bearerToken(operatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("No fields to update")));
    }

    @Test
    void shouldReturn400WhenNameIsBlank() throws Exception {
        String customerId = createCustomer("Maria Silva", VALID_CPF, "maria@example.com");

        mockMvc.perform(patch("/api/v1/customers/{id}", customerId)
                        .with(bearerToken(operatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());

        mockMvc.perform(patch("/api/v1/customers/{id}", customerId)
                        .with(bearerToken(operatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "   "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("blank")));
    }

    @Test
    void shouldReturn400WhenCustomerIdIsInvalid() throws Exception {
        mockMvc.perform(patch("/api/v1/customers/{id}", "not-a-uuid")
                        .with(bearerToken(operatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Maria Santos"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void shouldReturn401WhenUpdateCustomerCalledWithoutToken() throws Exception {
        mockMvc.perform(patch("/api/v1/customers/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Maria Santos"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value("https://api.financial-platform.lab/problems/invalid-token"))
                .andExpect(jsonPath("$.title").value("Authentication required"));
    }

    @Test
    void shouldReturn200WhenOnlyEmailIsUpdatedPreservingName() throws Exception {
        String customerId = createCustomer("Maria Silva", VALID_CPF, "maria@example.com");

        mockMvc.perform(patch("/api/v1/customers/{id}", customerId)
                        .with(bearerToken(operatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "maria.santos@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Maria Silva"))
                .andExpect(jsonPath("$.data.email").value("maria.santos@example.com"));
    }

    @Test
    void shouldReturn200WhenOnlyNameIsUpdatedPreservingEmail() throws Exception {
        String customerId = createCustomer("Maria Silva", VALID_CPF, "maria@example.com");

        mockMvc.perform(patch("/api/v1/customers/{id}", customerId)
                        .with(bearerToken(operatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Maria Santos"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Maria Santos"))
                .andExpect(jsonPath("$.data.email").value("maria@example.com"));
    }

    @Test
    void shouldReturn400WhenEmailIsInvalid() throws Exception {
        String customerId = createCustomer("Maria Silva", VALID_CPF, "maria@example.com");

        mockMvc.perform(patch("/api/v1/customers/{id}", customerId)
                        .with(bearerToken(operatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "invalid-email"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());
    }

    private String createCustomer(String name, String document, String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/customers")
                        .with(bearerToken(operatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "type": "INDIVIDUAL",
                                  "document": "%s",
                                  "email": "%s"
                                }
                                """.formatted(name, document, email)))
                .andExpect(status().isCreated())
                .andReturn();

        return com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.data.id");
    }
}
