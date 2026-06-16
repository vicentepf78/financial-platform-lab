package com.financialplatform.customer.features.updatecustomer;

import com.financialplatform.customer.domain.CustomerType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateCustomerRequest(
        @Size(min = 1, message = "Name must not be blank")
        String name,
        @Email(message = "Invalid email format")
        String email,
        @Null(message = "Document cannot be updated")
        String document,
        @Null(message = "Customer type cannot be updated")
        CustomerType type,
        @Null(message = "Id cannot be updated")
        UUID id) {
}
