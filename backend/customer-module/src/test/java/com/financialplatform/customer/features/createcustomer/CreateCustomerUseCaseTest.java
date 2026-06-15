package com.financialplatform.customer.features.createcustomer;

import com.financialplatform.customer.domain.Customer;
import com.financialplatform.customer.domain.CustomerType;
import com.financialplatform.customer.domain.DuplicateDocumentException;
import com.financialplatform.customer.ports.CustomerRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCustomerUseCaseTest {

    private static final String VALID_CPF = "529.982.247-25";
    private static final Instant FIXED_TIME = Instant.parse("2026-06-15T10:00:00Z");

    @Mock
    private CustomerRepositoryPort customerRepository;

    private CreateCustomerUseCase useCase;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(FIXED_TIME, ZoneOffset.UTC);
        useCase = new CreateCustomerUseCase(customerRepository, clock);
    }

    @Test
    void shouldCreateCustomerWhenDocumentIsUnique() {
        when(customerRepository.existsByDocument("52998224725")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateCustomerResult result = useCase.execute(new CreateCustomerCommand(
                "Maria Silva",
                CustomerType.INDIVIDUAL,
                VALID_CPF,
                "maria@example.com",
                null));

        assertThat(result.name()).isEqualTo("Maria Silva");
        assertThat(result.type()).isEqualTo(CustomerType.INDIVIDUAL);
        assertThat(result.document()).isEqualTo("529.982.247-25");
        assertThat(result.email()).isEqualTo("maria@example.com");
        assertThat(result.createdAt()).isEqualTo(FIXED_TIME);
        assertThat(result.id()).isNotNull();

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(captor.capture());
        assertThat(captor.getValue().createdBy()).isEqualTo("system");
    }

    @Test
    void shouldUseProvidedActorWhenActorIsPresent() {
        when(customerRepository.existsByDocument("52998224725")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(new CreateCustomerCommand(
                "Maria Silva",
                CustomerType.INDIVIDUAL,
                VALID_CPF,
                "maria@example.com",
                "operator-1"));

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(captor.capture());
        assertThat(captor.getValue().createdBy()).isEqualTo("operator-1");
    }

    @Test
    void shouldThrowDuplicateDocumentExceptionWhenDocumentExists() {
        when(customerRepository.existsByDocument("52998224725")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(new CreateCustomerCommand(
                "Maria Silva",
                CustomerType.INDIVIDUAL,
                VALID_CPF,
                "maria@example.com",
                null)))
                .isInstanceOf(DuplicateDocumentException.class);

        verify(customerRepository, never()).save(any());
    }

    @Test
    void shouldCreateCompanyCustomerWhenCnpjIsValid() {
        when(customerRepository.existsByDocument("11222333000181")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateCustomerResult result = useCase.execute(new CreateCustomerCommand(
                "Acme Ltda",
                CustomerType.COMPANY,
                "11.222.333/0001-81",
                "contato@acme.com",
                null));

        assertThat(result.type()).isEqualTo(CustomerType.COMPANY);
        assertThat(result.document()).isEqualTo("11.222.333/0001-81");
    }

    @Test
    void shouldRejectInvalidDocumentBeforeCheckingRepository() {
        assertThatThrownBy(() -> useCase.execute(new CreateCustomerCommand(
                "Maria",
                CustomerType.INDIVIDUAL,
                "111.111.111-11",
                "maria@example.com",
                null)))
                .isInstanceOf(IllegalArgumentException.class);

        verify(customerRepository, never()).existsByDocument(any());
        verify(customerRepository, never()).save(any());
    }
}
