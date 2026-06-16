package com.financialplatform.customer.features.updatecustomer;

import com.financialplatform.customer.domain.Customer;
import com.financialplatform.customer.domain.CustomerNotFoundException;
import com.financialplatform.customer.domain.CustomerType;
import com.financialplatform.customer.ports.CustomerRepositoryPort;
import com.financialplatform.sharedkernel.domain.Identifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateCustomerUseCaseTest {

    private static final String VALID_CPF = "529.982.247-25";
    private static final Instant CREATED_AT = Instant.parse("2026-06-15T10:00:00Z");
    private static final Instant UPDATED_AT = Instant.parse("2026-06-16T14:30:00Z");

    @Mock
    private CustomerRepositoryPort customerRepository;

    private UpdateCustomerUseCase useCase;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(UPDATED_AT, ZoneOffset.UTC);
        useCase = new UpdateCustomerUseCase(customerRepository, clock);
    }

    @Test
    void shouldUpdateCustomerWhenCustomerExists() {
        Customer customer = existingCustomer();
        when(customerRepository.findById(customer.id())).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateCustomerResult result = useCase.execute(new UpdateCustomerCommand(
                customer.id(),
                "Maria Santos",
                "maria.santos@example.com",
                null));

        assertThat(result.id()).isEqualTo(customer.id().value());
        assertThat(result.name()).isEqualTo("Maria Santos");
        assertThat(result.email()).isEqualTo("maria.santos@example.com");
        assertThat(result.type()).isEqualTo(CustomerType.INDIVIDUAL);
        assertThat(result.document()).isEqualTo("529.982.247-25");
        assertThat(result.createdAt()).isEqualTo(CREATED_AT);
        assertThat(result.updatedAt()).isEqualTo(UPDATED_AT);
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenCustomerDoesNotExist() {
        UUID missingId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        Identifier identifier = Identifier.of(missingId);
        when(customerRepository.findById(identifier)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new UpdateCustomerCommand(
                identifier,
                "Maria Santos",
                null,
                null)))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining(missingId.toString());

        verify(customerRepository, never()).save(any());
    }

    @Test
    void shouldUpdateNameOnlyPreservingEmail() {
        Customer customer = existingCustomer();
        when(customerRepository.findById(customer.id())).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateCustomerResult result = useCase.execute(new UpdateCustomerCommand(
                customer.id(),
                "Maria Santos",
                null,
                null));

        assertThat(result.name()).isEqualTo("Maria Santos");
        assertThat(result.email()).isEqualTo("maria@example.com");
    }

    @Test
    void shouldUseProvidedActorWhenActorIsPresent() {
        Customer customer = existingCustomer();
        when(customerRepository.findById(customer.id())).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(new UpdateCustomerCommand(
                customer.id(),
                "Maria Santos",
                null,
                "operator-1"));

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(captor.capture());
        assertThat(captor.getValue().updatedBy()).isEqualTo("operator-1");
    }

    @Test
    void shouldUpdateEmailOnlyPreservingName() {
        Customer customer = existingCustomer();
        when(customerRepository.findById(customer.id())).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateCustomerResult result = useCase.execute(new UpdateCustomerCommand(
                customer.id(),
                null,
                "maria.santos@example.com",
                null));

        assertThat(result.name()).isEqualTo("Maria Silva");
        assertThat(result.email()).isEqualTo("maria.santos@example.com");
    }

    private Customer existingCustomer() {
        return Customer.create(
                "Maria Silva",
                CustomerType.INDIVIDUAL,
                VALID_CPF,
                "maria@example.com",
                "system",
                CREATED_AT);
    }
}
