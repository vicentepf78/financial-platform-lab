package com.financialplatform.customer.features.createcustomer;

import com.financialplatform.customer.domain.CustomerType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCustomerRequest(
        @NotBlank(message = "Name is required")
        String name,
        @NotNull(message = "Type is required")
        CustomerType type,
        @NotBlank(message = "Document is required")
        String document,
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email) {
}
