package com.financialplatform.customer.features.querycustomers;

import com.financialplatform.sharedkernel.domain.Identifier;

import java.util.Objects;

public record GetCustomerByIdQuery(Identifier customerId) {

    public GetCustomerByIdQuery {
        Objects.requireNonNull(customerId, "Customer id is required");
    }
}
