package com.financialplatform.customer.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerUpdateTest {

    private static final String VALID_CPF = "529.982.247-25";
    private static final Instant CREATED_AT = Instant.parse("2026-06-15T10:00:00Z");
    private static final Instant UPDATED_AT = Instant.parse("2026-06-16T14:30:00Z");

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = Customer.create(
                "Maria Silva",
                CustomerType.INDIVIDUAL,
                VALID_CPF,
                "maria@example.com",
                "system",
                CREATED_AT);
    }

    @Test
    void shouldUpdateNameOnlyPreservingEmail() {
        Customer updated = customer.update(
                "Maria Santos",
                null,
                null,
                null,
                "operator",
                UPDATED_AT);

        assertThat(updated.name()).isEqualTo("Maria Santos");
        assertThat(updated.email().value()).isEqualTo("maria@example.com");
        assertThat(updated.type()).isEqualTo(CustomerType.INDIVIDUAL);
        assertThat(updated.document()).isEqualTo(customer.document());
    }

    @Test
    void shouldUpdateEmailOnlyPreservingName() {
        Customer updated = customer.update(
                null,
                "maria.santos@example.com",
                null,
                null,
                "operator",
                UPDATED_AT);

        assertThat(updated.name()).isEqualTo("Maria Silva");
        assertThat(updated.email().value()).isEqualTo("maria.santos@example.com");
    }

    @Test
    void shouldUpdateBothNameAndEmail() {
        Customer updated = customer.update(
                "Maria Santos",
                "maria.santos@example.com",
                null,
                null,
                "operator",
                UPDATED_AT);

        assertThat(updated.name()).isEqualTo("Maria Santos");
        assertThat(updated.email().value()).isEqualTo("maria.santos@example.com");
    }

    @Test
    void shouldTouchAuditFieldsOnUpdate() {
        Customer updated = customer.update(
                "Maria Santos",
                null,
                null,
                null,
                "operator",
                UPDATED_AT);

        assertThat(updated.createdAt()).isEqualTo(CREATED_AT);
        assertThat(updated.createdBy()).isEqualTo("system");
        assertThat(updated.updatedAt()).isEqualTo(UPDATED_AT);
        assertThat(updated.updatedBy()).isEqualTo("operator");
        assertThat(customer.updatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void shouldRejectUpdateWhenTypeIsProvided() {
        assertThatThrownBy(() -> customer.update(
                null,
                null,
                CustomerType.COMPANY,
                null,
                "operator",
                UPDATED_AT))
                .isInstanceOf(ImmutableFieldException.class)
                .hasMessageContaining("type");
    }

    @Test
    void shouldRejectUpdateWhenDocumentIsProvided() {
        assertThatThrownBy(() -> customer.update(
                null,
                null,
                null,
                "11.222.333/0001-81",
                "operator",
                UPDATED_AT))
                .isInstanceOf(ImmutableFieldException.class)
                .hasMessageContaining("document");
    }

    @Test
    void shouldRejectUpdateWhenNameIsBlank() {
        assertThatThrownBy(() -> customer.update(
                "  ",
                null,
                null,
                null,
                "operator",
                UPDATED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }
}
