package com.financialplatform.customer.features.querycustomers;

import java.util.Map;

public record GetCustomerByIdResponse(
        CustomerDetailResponse data,
        Map<String, Object> metadata) {

    public static GetCustomerByIdResponse from(CustomerDetailResult result) {
        return new GetCustomerByIdResponse(CustomerDetailResponse.from(result), Map.of());
    }
}
