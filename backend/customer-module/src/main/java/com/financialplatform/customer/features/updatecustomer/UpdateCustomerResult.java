package com.financialplatform.customer.features.updatecustomer;

import com.financialplatform.customer.domain.Customer;
import com.financialplatform.customer.domain.CustomerType;

import java.time.Instant;
import java.util.UUID;

public record UpdateCustomerResult(
        UUID id,
        String name,
        CustomerType type,
        String document,
        String email,
        Instant createdAt,
        Instant updatedAt) {

    public static UpdateCustomerResult from(Customer customer) {
        return new UpdateCustomerResult(
                customer.id().value(),
                customer.name(),
                customer.type(),
                customer.document().formatted(),
                customer.email().value(),
                customer.createdAt(),
                customer.updatedAt());
    }
}
