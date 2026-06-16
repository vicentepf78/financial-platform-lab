package com.financialplatform.customer.features.createcustomer;

import com.financialplatform.customer.support.AbstractCustomerIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static com.financialplatform.customer.support.JwtTestSupport.bearerToken;
import static com.financialplatform.customer.support.JwtTestSupport.obtainOperatorToken;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class CreateCustomerControllerIntegrationTest extends AbstractCustomerIntegrationTest {

    private static final String VALID_CPF = "529.982.247-25";

    @Autowired
    private MockMvc mockMvc;

    private String operatorToken;

    @BeforeEach
    void authenticate() throws Exception {
        operatorToken = obtainOperatorToken(mockMvc);
    }

    @Test
    void shouldReturn201WhenCustomerIsValid() throws Exception {
        mockMvc.perform(post("/api/v1/customers")
                        .with(bearerToken(operatorToken))
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
                .andExpect(jsonPath("$.data.id").value(notNullValue()))
                .andExpect(jsonPath("$.data.name").value("Maria Silva"))
                .andExpect(jsonPath("$.data.type").value("INDIVIDUAL"))
                .andExpect(jsonPath("$.data.document").value("529.982.247-25"))
                .andExpect(jsonPath("$.data.email").value("maria@example.com"))
                .andExpect(jsonPath("$.data.createdAt").value(notNullValue()))
                .andExpect(jsonPath("$.metadata").exists());
    }

    @Test
    void shouldReturn400WhenDocumentIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/customers")
                        .with(bearerToken(operatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Maria Silva",
                                  "type": "INDIVIDUAL",
                                  "document": "111.111.111-11",
                                  "email": "maria@example.com"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void shouldReturn400WhenTypeAndDocumentAreInconsistent() throws Exception {
        mockMvc.perform(post("/api/v1/customers")
                        .with(bearerToken(operatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Acme Ltda",
                                  "type": "COMPANY",
                                  "document": "%s",
                                  "email": "contato@acme.com"
                                }
                                """.formatted(VALID_CPF)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void shouldReturn409WhenDocumentIsDuplicated() throws Exception {
        String body = """
                {
                  "name": "Maria Silva",
                  "type": "INDIVIDUAL",
                  "document": "%s",
                  "email": "maria@example.com"
                }
                """.formatted(VALID_CPF);

        mockMvc.perform(post("/api/v1/customers")
                        .with(bearerToken(operatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/customers")
                        .with(bearerToken(operatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("already exists")));
    }

    @Test
    void shouldReturn400WhenRequestValidationFails() throws Exception {
        mockMvc.perform(post("/api/v1/customers")
                        .with(bearerToken(operatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "type": "INDIVIDUAL",
                                  "document": "%s",
                                  "email": "invalid-email"
                                }
                                """.formatted(VALID_CPF)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());
    }
}
