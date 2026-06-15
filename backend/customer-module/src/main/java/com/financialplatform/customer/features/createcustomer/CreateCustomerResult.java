package com.financialplatform.customer.features.createcustomer;

import com.financialplatform.customer.domain.Customer;
import com.financialplatform.customer.domain.CustomerType;

import java.time.Instant;
import java.util.UUID;

public record CreateCustomerResult(
        UUID id,
        String name,
        CustomerType type,
        String document,
        String email,
        Instant createdAt) {

    public static CreateCustomerResult from(Customer customer) {
        return new CreateCustomerResult(
                customer.id().value(),
                customer.name(),
                customer.type(),
                customer.document().formatted(),
                customer.email().value(),
                customer.createdAt());
    }
}
