package com.financialplatform.customer.adapters.persistence;

import com.financialplatform.customer.domain.Customer;
import com.financialplatform.customer.domain.CustomerType;
import com.financialplatform.customer.ports.CustomerRepositoryPort;
import com.financialplatform.customer.support.AbstractCustomerIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class JpaCustomerRepositoryIntegrationTest extends AbstractCustomerIntegrationTest {

    private static final String VALID_CPF = "529.982.247-25";

    @Autowired
    private CustomerRepositoryPort repository;

    @Test
    void shouldPersistCustomerWhenDocumentIsUnique() {
        Customer customer = Customer.create(
                "Maria Silva",
                CustomerType.INDIVIDUAL,
                VALID_CPF,
                "maria@example.com",
                "system",
                Instant.parse("2026-06-15T10:00:00Z"));

        Customer saved = repository.save(customer);

        assertThat(saved.id()).isEqualTo(customer.id());
        assertThat(repository.existsByDocument("52998224725")).isTrue();
    }

    @Test
    void shouldFindCustomerByIdWhenCustomerExists() {
        Customer customer = Customer.create(
                "João Santos",
                CustomerType.INDIVIDUAL,
                "390.533.447-05",
                "joao@example.com",
                "system",
                Instant.parse("2026-06-15T11:00:00Z"));

        repository.save(customer);

        assertThat(repository.findById(customer.id())).isPresent();
    }
}
