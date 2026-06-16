package com.financialplatform.customer.application.readmodel;

import com.financialplatform.customer.domain.CustomerType;

import java.util.Optional;

public record CustomerFilter(
        Optional<String> name,
        Optional<CustomerType> type,
        Optional<String> documentDigits) {
}
