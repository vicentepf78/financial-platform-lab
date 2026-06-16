package com.financialplatform.customer.features.querycustomers;

import com.financialplatform.customer.application.readmodel.CustomerSummary;
import com.financialplatform.customer.application.readmodel.PageResult;

import java.util.List;

public record QueryCustomersResponse(
        List<CustomerSummaryResponse> data,
        PaginationMetadata metadata) {

    public static QueryCustomersResponse from(PageResult<CustomerSummary> pageResult) {
        List<CustomerSummaryResponse> items = pageResult.content().stream()
                .map(CustomerSummaryResponse::from)
                .toList();
        return new QueryCustomersResponse(items, PaginationMetadata.from(pageResult));
    }
}
