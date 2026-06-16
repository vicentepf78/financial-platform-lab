package com.financialplatform.customer.adapters.persistence;

import com.financialplatform.customer.application.readmodel.CustomerFilter;
import com.financialplatform.customer.application.readmodel.CustomerSummary;
import com.financialplatform.customer.application.readmodel.PageRequest;
import com.financialplatform.customer.application.readmodel.PageResult;
import com.financialplatform.customer.domain.Customer;
import com.financialplatform.customer.domain.CustomerType;
import com.financialplatform.customer.ports.CustomerQueryPort;
import com.financialplatform.customer.ports.CustomerRepositoryPort;
import com.financialplatform.customer.support.AbstractCustomerIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JpaCustomerQueryAdapterIntegrationTest extends AbstractCustomerIntegrationTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2026-06-15T10:00:00Z");

    @Autowired
    private CustomerQueryPort customerQueryPort;

    @Autowired
    private CustomerRepositoryPort customerRepository;

    @Test
    void shouldReturnPaginatedCustomersWhenMultipleCustomersExist() {
        for (int i = 0; i < 5; i++) {
            saveCustomer("Customer " + i, CustomerType.INDIVIDUAL, cpfForIndex(i), "customer" + i + "@example.com");
        }

        PageResult<CustomerSummary> page0 = customerQueryPort.findAll(
                emptyFilter(),
                new PageRequest(0, 2));

        assertThat(page0.content()).hasSize(2);
        assertThat(page0.page()).isZero();
        assertThat(page0.size()).isEqualTo(2);
        assertThat(page0.totalElements()).isEqualTo(5);
        assertThat(page0.totalPages()).isEqualTo(3);

        PageResult<CustomerSummary> page2 = customerQueryPort.findAll(
                emptyFilter(),
                new PageRequest(2, 2));

        assertThat(page2.content()).hasSize(1);
        assertThat(page2.totalElements()).isEqualTo(5);
    }

    @Test
    void shouldFilterCustomersByNameWhenNameFilterIsProvided() {
        saveCustomer("Maria Silva", CustomerType.INDIVIDUAL, "529.982.247-25", "maria@example.com");
        saveCustomer("Maria Santos", CustomerType.INDIVIDUAL, "390.533.447-05", "santos@example.com");
        saveCustomer("João Pereira", CustomerType.INDIVIDUAL, "231.002.999-81", "joao@example.com");

        CustomerFilter filter = new CustomerFilter(Optional.of("maria"), Optional.empty(), Optional.empty());
        PageResult<CustomerSummary> result = customerQueryPort.findAll(filter, new PageRequest(0, 20));

        assertThat(result.content()).hasSize(2);
        assertThat(result.content()).extracting(CustomerSummary::name)
                .containsExactlyInAnyOrder("Maria Silva", "Maria Santos");
    }

    @Test
    void shouldFilterCustomersByTypeWhenTypeFilterIsProvided() {
        saveCustomer("PF One", CustomerType.INDIVIDUAL, "529.982.247-25", "pf1@example.com");
        saveCustomer("PF Two", CustomerType.INDIVIDUAL, "390.533.447-05", "pf2@example.com");
        saveCustomer("PJ One", CustomerType.COMPANY, "11.222.333/0001-81", "pj@example.com");

        CustomerFilter filter = new CustomerFilter(Optional.empty(), Optional.of(CustomerType.INDIVIDUAL), Optional.empty());
        PageResult<CustomerSummary> result = customerQueryPort.findAll(filter, new PageRequest(0, 20));

        assertThat(result.content()).hasSize(2);
        assertThat(result.content()).allMatch(summary -> summary.type() == CustomerType.INDIVIDUAL);
    }

    @Test
    void shouldFilterCustomersByDocumentWhenDocumentHasFormatting() {
        saveCustomer("Maria Silva", CustomerType.INDIVIDUAL, "529.982.247-25", "maria@example.com");
        saveCustomer("João Santos", CustomerType.INDIVIDUAL, "390.533.447-05", "joao@example.com");

        CustomerFilter filter = new CustomerFilter(
                Optional.empty(),
                Optional.empty(),
                Optional.of("529.982.247-25"));
        PageResult<CustomerSummary> result = customerQueryPort.findAll(filter, new PageRequest(0, 20));

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().name()).isEqualTo("Maria Silva");
        assertThat(result.content().getFirst().documentFormatted()).isEqualTo("529.982.247-25");
    }

    @Test
    void shouldApplyAndLogicWhenMultipleFiltersAreProvided() {
        saveCustomer("Maria Silva", CustomerType.INDIVIDUAL, "529.982.247-25", "maria@example.com");
        saveCustomer("Maria Corp", CustomerType.COMPANY, "11.222.333/0001-81", "corp@example.com");
        saveCustomer("João Silva", CustomerType.INDIVIDUAL, "390.533.447-05", "joao@example.com");

        CustomerFilter filter = new CustomerFilter(
                Optional.of("Maria"),
                Optional.of(CustomerType.INDIVIDUAL),
                Optional.empty());
        PageResult<CustomerSummary> result = customerQueryPort.findAll(filter, new PageRequest(0, 20));

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().name()).isEqualTo("Maria Silva");
    }

    @Test
    void shouldReturnCustomersOrderedByCreatedAtDescending() {
        Instant oldest = FIXED_INSTANT.minusSeconds(200);
        Instant middle = FIXED_INSTANT.minusSeconds(100);
        Instant newest = FIXED_INSTANT;

        saveCustomer("Oldest", CustomerType.INDIVIDUAL, "529.982.247-25", "oldest@example.com", oldest);
        saveCustomer("Newest", CustomerType.INDIVIDUAL, "390.533.447-05", "newest@example.com", newest);
        saveCustomer("Middle", CustomerType.INDIVIDUAL, "231.002.999-81", "middle@example.com", middle);

        PageResult<CustomerSummary> result = customerQueryPort.findAll(emptyFilter(), new PageRequest(0, 10));

        assertThat(result.content()).extracting(CustomerSummary::name)
                .containsExactly("Newest", "Middle", "Oldest");
        assertThat(result.content()).extracting(CustomerSummary::createdAt)
                .containsExactly(newest, middle, oldest);
    }

    @Test
    void shouldFindCustomerByIdWhenCustomerExists() {
        Customer customer = saveCustomer(
                "Ana Costa",
                CustomerType.INDIVIDUAL,
                "231.002.999-81",
                "ana@example.com");

        assertThat(customerQueryPort.findById(customer.id())).isPresent();
    }

    private Customer saveCustomer(String name, CustomerType type, String document, String email) {
        return saveCustomer(name, type, document, email, FIXED_INSTANT);
    }

    private Customer saveCustomer(String name, CustomerType type, String document, String email, Instant createdAt) {
        Customer customer = Customer.create(name, type, document, email, "system", createdAt);
        return customerRepository.save(customer);
    }

    private static CustomerFilter emptyFilter() {
        return new CustomerFilter(Optional.empty(), Optional.empty(), Optional.empty());
    }

    private static String cpfForIndex(int index) {
        return switch (index) {
            case 0 -> "529.982.247-25";
            case 1 -> "390.533.447-05";
            case 2 -> "231.002.999-81";
            case 3 -> "123.456.789-09";
            case 4 -> "987.654.321-00";
            default -> throw new IllegalArgumentException("Unsupported index: " + index);
        };
    }
}
