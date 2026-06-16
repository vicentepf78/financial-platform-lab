package com.financialplatform.customer.features.querycustomers;

import com.financialplatform.customer.support.AbstractCustomerIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class QueryCustomersControllerIntegrationTest extends AbstractCustomerIntegrationTest {

    private static final String VALID_CPF = "529.982.247-25";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturn200WithEmptyListWhenNoCustomersExist() throws Exception {
        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.metadata.page").value(0))
                .andExpect(jsonPath("$.metadata.size").value(20))
                .andExpect(jsonPath("$.metadata.totalElements").value(0))
                .andExpect(jsonPath("$.metadata.totalPages").value(0));
    }

    @Test
    void shouldReturn200WithPaginatedCustomersWhenCustomersExist() throws Exception {
        createCustomer("Maria Silva", VALID_CPF, "maria@example.com");
        createCustomer("João Pereira", "390.533.447-05", "joao@example.com");

        mockMvc.perform(get("/api/v1/customers").param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name").value(notNullValue()))
                .andExpect(jsonPath("$.metadata.page").value(0))
                .andExpect(jsonPath("$.metadata.size").value(1))
                .andExpect(jsonPath("$.metadata.totalElements").value(2))
                .andExpect(jsonPath("$.metadata.totalPages").value(2));
    }

    @Test
    void shouldReturn200WithFilteredCustomersWhenNameFilterIsProvided() throws Exception {
        createCustomer("Maria Silva", VALID_CPF, "maria@example.com");
        createCustomer("João Pereira", "390.533.447-05", "joao@example.com");

        mockMvc.perform(get("/api/v1/customers").param("name", "maria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name").value("Maria Silva"));
    }

    @Test
    void shouldReturn200WithFilteredCustomersWhenTypeFilterIsProvided() throws Exception {
        createCustomer("Maria Silva", VALID_CPF, "maria@example.com");
        createCompanyCustomer("Acme Ltda", "11.222.333/0001-81", "contato@acme.com");

        mockMvc.perform(get("/api/v1/customers").param("type", "INDIVIDUAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name").value("Maria Silva"))
                .andExpect(jsonPath("$.data[0].type").value("INDIVIDUAL"));
    }

    @Test
    void shouldReturn200WithFilteredCustomersWhenDocumentFilterIsProvided() throws Exception {
        createCustomer("Maria Silva", VALID_CPF, "maria@example.com");
        createCustomer("João Pereira", "390.533.447-05", "joao@example.com");

        mockMvc.perform(get("/api/v1/customers").param("document", VALID_CPF))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name").value("Maria Silva"))
                .andExpect(jsonPath("$.data[0].document").value(VALID_CPF));
    }

    @Test
    void shouldReturn400WhenPageIsNegative() throws Exception {
        mockMvc.perform(get("/api/v1/customers").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void shouldReturn400WhenSizeIsZero() throws Exception {
        mockMvc.perform(get("/api/v1/customers").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void shouldReturn200WithCustomerDetailWhenCustomerExists() throws Exception {
        String customerId = createCustomer("Maria Silva", VALID_CPF, "maria@example.com");

        mockMvc.perform(get("/api/v1/customers/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(customerId))
                .andExpect(jsonPath("$.data.name").value("Maria Silva"))
                .andExpect(jsonPath("$.data.type").value("INDIVIDUAL"))
                .andExpect(jsonPath("$.data.document").value(VALID_CPF))
                .andExpect(jsonPath("$.data.email").value("maria@example.com"))
                .andExpect(jsonPath("$.data.createdAt").value(notNullValue()))
                .andExpect(jsonPath("$.data.updatedAt").value(notNullValue()))
                .andExpect(jsonPath("$.metadata").exists());
    }

    @Test
    void shouldReturn404WhenCustomerDoesNotExist() throws Exception {
        UUID missingId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        mockMvc.perform(get("/api/v1/customers/{id}", missingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString(missingId.toString())));
    }

    @Test
    void shouldReturn400WhenCustomerIdIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/customers/{id}", "not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());
    }

    private String createCustomer(String name, String document, String email) throws Exception {
        return createCustomerWithType(name, "INDIVIDUAL", document, email);
    }

    private String createCompanyCustomer(String name, String document, String email) throws Exception {
        return createCustomerWithType(name, "COMPANY", document, email);
    }

    private String createCustomerWithType(String name, String type, String document, String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "type": "%s",
                                  "document": "%s",
                                  "email": "%s"
                                }
                                """.formatted(name, type, document, email)))
                .andExpect(status().isCreated())
                .andReturn();

        return com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.data.id");
    }
}
