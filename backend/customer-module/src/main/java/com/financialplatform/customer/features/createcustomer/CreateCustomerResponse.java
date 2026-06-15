package com.financialplatform.customer.features.createcustomer;

import com.financialplatform.customer.domain.CustomerType;

import java.time.Instant;
import java.util.UUID;

public record CreateCustomerResponse(
        UUID id,
        String name,
        CustomerType type,
        String document,
        String email,
        Instant createdAt) {

    public static CreateCustomerResponse from(CreateCustomerResult result) {
        return new CreateCustomerResponse(
                result.id(),
                result.name(),
                result.type(),
                result.document(),
                result.email(),
                result.createdAt());
    }
}
