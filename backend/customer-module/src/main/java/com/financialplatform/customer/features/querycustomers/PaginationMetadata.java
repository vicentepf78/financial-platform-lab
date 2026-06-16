package com.financialplatform.customer.features.querycustomers;

import com.financialplatform.customer.application.readmodel.PageResult;

public record PaginationMetadata(
        int page,
        int size,
        long totalElements,
        int totalPages) {

    public static PaginationMetadata from(PageResult<?> pageResult) {
        return new PaginationMetadata(
                pageResult.page(),
                pageResult.size(),
                pageResult.totalElements(),
                pageResult.totalPages());
    }
}
