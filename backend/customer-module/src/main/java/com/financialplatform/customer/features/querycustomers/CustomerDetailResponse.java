package com.financialplatform.customer.features.querycustomers;

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

    public static CustomerDetailResponse from(CustomerDetailResult result) {
        return new CustomerDetailResponse(
                result.id(),
                result.name(),
                result.type(),
                result.document(),
                result.email(),
                result.createdAt(),
                result.updatedAt());
    }
}
