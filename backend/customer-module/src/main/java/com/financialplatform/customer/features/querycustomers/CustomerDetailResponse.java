package com.financialplatform.customer.features.querycustomers;

import com.financialplatform.customer.domain.Customer;
import com.financialplatform.customer.domain.CustomerType;

import java.time.Instant;
import java.util.UUID;

public record CustomerDetailResponse(
        UUID id,
        String name,
        CustomerType type,
        String document,
        String email,
        Instant createdAt,
        Instant updatedAt) {

    public static CustomerDetailResponse from(Customer customer) {
        return new CustomerDetailResponse(
                customer.id().value(),
                customer.name(),
                customer.type(),
                customer.document().formatted(),
                customer.email().value(),
                customer.createdAt(),
                customer.updatedAt());
    }
}
