package com.financialplatform.customer.features.querycustomers;

import com.financialplatform.customer.application.readmodel.CustomerSummary;
import com.financialplatform.customer.domain.CustomerType;

import java.time.Instant;
import java.util.UUID;

public record CustomerSummaryResponse(
        UUID id,
        String name,
        CustomerType type,
        String document,
        String email,
        Instant createdAt) {

    public static CustomerSummaryResponse from(CustomerSummary summary) {
        return new CustomerSummaryResponse(
                summary.id(),
                summary.name(),
                summary.type(),
                summary.documentFormatted(),
                summary.email(),
                summary.createdAt());
    }
}
