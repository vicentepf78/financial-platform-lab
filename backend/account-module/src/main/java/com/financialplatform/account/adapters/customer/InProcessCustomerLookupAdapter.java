package com.financialplatform.account.adapters.customer;

import com.financialplatform.account.ports.CustomerLookupPort;
import com.financialplatform.customer.ports.CustomerRepositoryPort;
import com.financialplatform.sharedkernel.domain.Identifier;

import java.util.Objects;

public final class InProcessCustomerLookupAdapter implements CustomerLookupPort {

    private final CustomerRepositoryPort customerRepository;

    public InProcessCustomerLookupAdapter(CustomerRepositoryPort customerRepository) {
        this.customerRepository = Objects.requireNonNull(customerRepository, "CustomerRepositoryPort is required");
    }

    @Override
    public boolean exists(Identifier customerId) {
        Objects.requireNonNull(customerId, "CustomerId is required");
        return customerRepository.findById(customerId).isPresent();
    }
}
