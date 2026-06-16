package com.financialplatform.customer.features.updatecustomer;

import com.financialplatform.sharedkernel.domain.Identifier;

import java.util.Objects;

public record UpdateCustomerCommand(
        Identifier customerId,
        String name,
        String email,
        String actor) {

    public UpdateCustomerCommand {
        Objects.requireNonNull(customerId, "Customer id is required");
    }
}
