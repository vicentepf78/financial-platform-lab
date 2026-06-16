package com.financialplatform.customer.application.readmodel;

import com.financialplatform.customer.domain.CustomerType;

import java.time.Instant;
import java.util.UUID;

public record CustomerSummary(
        UUID id,
        String name,
        CustomerType type,
        String documentFormatted,
        String email,
        Instant createdAt) {
}
