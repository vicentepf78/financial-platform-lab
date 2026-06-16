package com.financialplatform.customer.features.querycustomers;

import com.financialplatform.customer.domain.CustomerType;

import java.util.Optional;

public record QueryCustomersQuery(
        Optional<Integer> page,
        Optional<Integer> size,
        Optional<String> name,
        Optional<CustomerType> type,
        Optional<String> document) {
}
