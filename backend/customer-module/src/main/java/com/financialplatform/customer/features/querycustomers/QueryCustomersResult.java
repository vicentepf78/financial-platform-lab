package com.financialplatform.customer.features.querycustomers;

import com.financialplatform.customer.application.readmodel.CustomerSummary;
import com.financialplatform.customer.application.readmodel.PageResult;

import java.util.List;

public record QueryCustomersResult(
        List<CustomerSummary> content,
        PaginationMetadata metadata) {

    public static QueryCustomersResult from(PageResult<CustomerSummary> pageResult) {
        return new QueryCustomersResult(pageResult.content(), PaginationMetadata.from(pageResult));
    }
}
