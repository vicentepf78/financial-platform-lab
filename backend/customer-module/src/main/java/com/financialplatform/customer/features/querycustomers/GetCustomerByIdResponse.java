package com.financialplatform.customer.features.querycustomers;

import com.financialplatform.customer.domain.Customer;

import java.util.Map;

public record GetCustomerByIdResponse(
        CustomerDetailResponse data,
        Map<String, Object> metadata) {

    public static GetCustomerByIdResponse from(Customer customer) {
        return new GetCustomerByIdResponse(CustomerDetailResponse.from(customer), Map.of());
    }
}
