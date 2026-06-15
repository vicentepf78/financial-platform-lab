package com.financialplatform.account.adapters.customer;

import com.financialplatform.customer.domain.Customer;
import com.financialplatform.customer.domain.CustomerType;
import com.financialplatform.customer.domain.Document;
import com.financialplatform.customer.domain.Email;
import com.financialplatform.customer.ports.CustomerRepositoryPort;
import com.financialplatform.sharedkernel.domain.Identifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InProcessCustomerLookupAdapterTest {

    private static final Identifier CUSTOMER_ID = Identifier.of("c1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final Instant FIXED_TIME = Instant.parse("2026-06-15T10:00:00Z");

    @Mock
    private CustomerRepositoryPort customerRepository;

    private InProcessCustomerLookupAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new InProcessCustomerLookupAdapter(customerRepository);
    }

    @Test
    void shouldReturnTrueWhenCustomerExists() {
        Customer customer = Customer.reconstitute(
                CUSTOMER_ID,
                "Maria Silva",
                CustomerType.INDIVIDUAL,
                Document.of(CustomerType.INDIVIDUAL, "52998224725"),
                Email.of("maria@example.com"),
                FIXED_TIME,
                "system",
                FIXED_TIME,
                "system");
        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer));

        boolean exists = adapter.exists(CUSTOMER_ID);

        assertThat(exists).isTrue();
        verify(customerRepository).findById(CUSTOMER_ID);
    }

    @Test
    void shouldReturnFalseWhenCustomerDoesNotExist() {
        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.empty());

        boolean exists = adapter.exists(CUSTOMER_ID);

        assertThat(exists).isFalse();
        verify(customerRepository).findById(CUSTOMER_ID);
    }
}
