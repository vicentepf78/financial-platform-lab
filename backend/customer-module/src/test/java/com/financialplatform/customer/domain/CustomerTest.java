package com.financialplatform.customer.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerTest {

    private static final String VALID_CPF = "529.982.247-25";
    private static final Instant NOW = Instant.parse("2026-06-15T10:00:00Z");

    @Test
    void shouldCreateCustomerWhenDataIsValid() {
        Customer customer = Customer.create(
                "Maria Silva",
                CustomerType.INDIVIDUAL,
                VALID_CPF,
                "maria@example.com",
                "system",
                NOW);

        assertThat(customer.id()).isNotNull();
        assertThat(customer.name()).isEqualTo("Maria Silva");
        assertThat(customer.type()).isEqualTo(CustomerType.INDIVIDUAL);
        assertThat(customer.document().formatted()).isEqualTo("529.982.247-25");
        assertThat(customer.email().value()).isEqualTo("maria@example.com");
        assertThat(customer.createdAt()).isEqualTo(NOW);
        assertThat(customer.createdBy()).isEqualTo("system");
        assertThat(customer.updatedAt()).isEqualTo(NOW);
        assertThat(customer.updatedBy()).isEqualTo("system");
    }

    @Test
    void shouldRejectCustomerWhenNameIsBlank() {
        assertThatThrownBy(() -> Customer.create(
                "  ",
                CustomerType.INDIVIDUAL,
                VALID_CPF,
                "maria@example.com",
                "system",
                NOW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void shouldRejectCustomerWhenDocumentIsInvalid() {
        assertThatThrownBy(() -> Customer.create(
                "Maria",
                CustomerType.INDIVIDUAL,
                "111.111.111-11",
                "maria@example.com",
                "system",
                NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
