package com.financialplatform.customer.features.querycustomers;

import com.financialplatform.customer.domain.Customer;
import com.financialplatform.customer.domain.CustomerNotFoundException;
import com.financialplatform.customer.domain.CustomerType;
import com.financialplatform.customer.ports.CustomerQueryPort;
import com.financialplatform.sharedkernel.domain.Identifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCustomerByIdUseCaseTest {

    private static final String VALID_CPF = "529.982.247-25";
    private static final Instant FIXED_TIME = Instant.parse("2026-06-15T10:00:00Z");

    @Mock
    private CustomerQueryPort customerQuery;

    private GetCustomerByIdUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetCustomerByIdUseCase(customerQuery);
    }

    @Test
    void shouldReturnCustomerDetailWhenCustomerExists() {
        Customer customer = Customer.create(
                "Maria Silva",
                CustomerType.INDIVIDUAL,
                VALID_CPF,
                "maria@example.com",
                "system",
                FIXED_TIME);
        when(customerQuery.findById(customer.id())).thenReturn(Optional.of(customer));

        CustomerDetailResult result = useCase.execute(new GetCustomerByIdQuery(customer.id()));

        assertThat(result.id()).isEqualTo(customer.id().value());
        assertThat(result.name()).isEqualTo("Maria Silva");
        assertThat(result.type()).isEqualTo(CustomerType.INDIVIDUAL);
        assertThat(result.document()).isEqualTo("529.982.247-25");
        assertThat(result.email()).isEqualTo("maria@example.com");
        assertThat(result.createdAt()).isEqualTo(FIXED_TIME);
        assertThat(result.updatedAt()).isEqualTo(FIXED_TIME);
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenCustomerDoesNotExist() {
        UUID missingId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        Identifier identifier = Identifier.of(missingId);
        when(customerQuery.findById(identifier)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new GetCustomerByIdQuery(identifier)))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining(missingId.toString());
    }

    @Test
    void shouldRejectNullQuery() {
        assertThatThrownBy(() -> useCase.execute(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Query is required");
    }

    @Test
    void shouldDelegateFindByIdToCustomerQueryPort() {
        Customer customer = Customer.create(
                "Acme Ltda",
                CustomerType.COMPANY,
                "11.222.333/0001-81",
                "contato@acme.com",
                "operator-1",
                FIXED_TIME);
        when(customerQuery.findById(customer.id())).thenReturn(Optional.of(customer));

        useCase.execute(new GetCustomerByIdQuery(customer.id()));

        verify(customerQuery).findById(customer.id());
    }
}
