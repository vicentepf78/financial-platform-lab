package com.financialplatform.customer.features.updatecustomer;

import com.financialplatform.customer.domain.CustomerType;

import java.time.Instant;
import java.util.UUID;

public record UpdateCustomerResponse(
        UUID id,
        String name,
        CustomerType type,
        String document,
        String email,
        Instant createdAt,
        Instant updatedAt) {

    public static UpdateCustomerResponse from(UpdateCustomerResult result) {
        return new UpdateCustomerResponse(
                result.id(),
                result.name(),
                result.type(),
                result.document(),
                result.email(),
                result.createdAt(),
                result.updatedAt());
    }
}
