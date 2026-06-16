package com.financialplatform.customer.features.querycustomers;

import java.util.List;

public record QueryCustomersResponse(
        List<CustomerSummaryResponse> data,
        PaginationMetadata metadata) {

    public static QueryCustomersResponse from(QueryCustomersResult result) {
        List<CustomerSummaryResponse> items = result.content().stream()
                .map(CustomerSummaryResponse::from)
                .toList();
        return new QueryCustomersResponse(items, result.metadata());
    }
}
