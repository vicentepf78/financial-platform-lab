package com.financialplatform.customer.ports;

import com.financialplatform.customer.domain.Customer;
import com.financialplatform.sharedkernel.domain.Identifier;

import java.util.Optional;

public interface CustomerRepositoryPort {

    boolean existsByDocument(String documentDigits);

    Customer save(Customer customer);

    Optional<Customer> findById(Identifier id);
}
