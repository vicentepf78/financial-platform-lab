package com.financialplatform.customer.features.createcustomer;

import com.financialplatform.customer.domain.CustomerType;

public record CreateCustomerCommand(
        String name,
        CustomerType type,
        String document,
        String email,
        String actor) {
}
