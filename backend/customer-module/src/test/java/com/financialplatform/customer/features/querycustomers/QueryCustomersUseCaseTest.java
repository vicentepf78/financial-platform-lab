package com.financialplatform.customer.features.querycustomers;

import com.financialplatform.customer.application.readmodel.CustomerFilter;
import com.financialplatform.customer.application.readmodel.CustomerSummary;
import com.financialplatform.customer.application.readmodel.PageRequest;
import com.financialplatform.customer.application.readmodel.PageResult;
import com.financialplatform.customer.domain.CustomerType;
import com.financialplatform.customer.ports.CustomerQueryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueryCustomersUseCaseTest {

    private static final Instant FIXED_TIME = Instant.parse("2026-06-15T10:00:00Z");

    @Mock
    private CustomerQueryPort customerQuery;

    private QueryCustomersUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new QueryCustomersUseCase(customerQuery);
    }

    @Test
    void shouldUseDefaultPaginationWhenPageAndSizeAreOmitted() {
        when(customerQuery.findAll(any(), any())).thenReturn(emptyPageResult(0, 20));

        useCase.execute(new QueryCustomersQuery(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()));

        ArgumentCaptor<PageRequest> pageCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(customerQuery).findAll(any(), pageCaptor.capture());
        assertThat(pageCaptor.getValue()).isEqualTo(new PageRequest(0, 20));
    }

    @Test
    void shouldCapSizeAtMaxWhenSizeExceedsLimit() {
        when(customerQuery.findAll(any(), any())).thenReturn(emptyPageResult(0, 100));

        useCase.execute(new QueryCustomersQuery(
                Optional.of(0),
                Optional.of(250),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()));

        ArgumentCaptor<PageRequest> pageCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(customerQuery).findAll(any(), pageCaptor.capture());
        assertThat(pageCaptor.getValue()).isEqualTo(new PageRequest(0, 100));
    }

    @Test
    void shouldReturnResultWithMetadataWhenQuerySucceeds() {
        CustomerSummary summary = sampleSummary("Maria Silva");
        PageResult<CustomerSummary> pageResult = new PageResult<>(
                List.of(summary),
                1,
                20,
                25,
                2);
        when(customerQuery.findAll(any(), any())).thenReturn(pageResult);

        QueryCustomersResult result = useCase.execute(new QueryCustomersQuery(
                Optional.of(1),
                Optional.of(20),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()));

        assertThat(result.content()).containsExactly(summary);
        assertThat(result.metadata().page()).isEqualTo(1);
        assertThat(result.metadata().size()).isEqualTo(20);
        assertThat(result.metadata().totalElements()).isEqualTo(25);
        assertThat(result.metadata().totalPages()).isEqualTo(2);
    }

    @Test
    void shouldPassCustomerFilterToQueryPortWhenFiltersAreProvided() {
        when(customerQuery.findAll(any(), any())).thenReturn(emptyPageResult(0, 20));

        useCase.execute(new QueryCustomersQuery(
                Optional.empty(),
                Optional.empty(),
                Optional.of("Maria"),
                Optional.of(CustomerType.INDIVIDUAL),
                Optional.of("529.982.247-25")));

        ArgumentCaptor<CustomerFilter> filterCaptor = ArgumentCaptor.forClass(CustomerFilter.class);
        verify(customerQuery).findAll(filterCaptor.capture(), any());
        assertThat(filterCaptor.getValue().name()).contains("Maria");
        assertThat(filterCaptor.getValue().type()).contains(CustomerType.INDIVIDUAL);
        assertThat(filterCaptor.getValue().documentDigits()).contains("529.982.247-25");
    }

    @Test
    void shouldThrowWhenPageIsNegative() {
        assertThatThrownBy(() -> useCase.execute(new QueryCustomersQuery(
                Optional.of(-1),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Page must not be negative");
    }

    @Test
    void shouldThrowWhenSizeIsZeroOrNegative() {
        assertThatThrownBy(() -> useCase.execute(new QueryCustomersQuery(
                Optional.empty(),
                Optional.of(0),
                Optional.empty(),
                Optional.empty(),
                Optional.empty())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Size must be positive");
    }

    private static PageResult<CustomerSummary> emptyPageResult(int page, int size) {
        return new PageResult<>(List.of(), page, size, 0, 0);
    }

    private static CustomerSummary sampleSummary(String name) {
        return new CustomerSummary(
                UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
                name,
                CustomerType.INDIVIDUAL,
                "529.982.247-25",
                "maria@example.com",
                FIXED_TIME);
    }
}
