package com.financialplatform.customer.features.querycustomers;

import com.financialplatform.customer.application.readmodel.CustomerFilter;
import com.financialplatform.customer.application.readmodel.PageRequest;
import com.financialplatform.customer.application.readmodel.PageResult;
import com.financialplatform.customer.application.readmodel.CustomerSummary;
import com.financialplatform.customer.ports.CustomerQueryPort;

import java.util.Objects;
import java.util.Optional;

public class QueryCustomersUseCase {

    static final int DEFAULT_PAGE = 0;
    static final int DEFAULT_SIZE = 20;
    static final int MAX_SIZE = 100;

    private final CustomerQueryPort customerQuery;

    public QueryCustomersUseCase(CustomerQueryPort customerQuery) {
        this.customerQuery = Objects.requireNonNull(customerQuery, "CustomerQueryPort is required");
    }

    public QueryCustomersResult execute(QueryCustomersQuery query) {
        Objects.requireNonNull(query, "Query is required");

        PageRequest pageRequest = new PageRequest(resolvePage(query.page()), resolveSize(query.size()));
        CustomerFilter filter = toFilter(query);

        PageResult<CustomerSummary> pageResult = customerQuery.findAll(filter, pageRequest);
        return QueryCustomersResult.from(pageResult);
    }

    private int resolvePage(Optional<Integer> page) {
        int resolved = page.orElse(DEFAULT_PAGE);
        if (resolved < 0) {
            throw new IllegalArgumentException("Page must not be negative");
        }
        return resolved;
    }

    private int resolveSize(Optional<Integer> size) {
        int resolved = size.orElse(DEFAULT_SIZE);
        if (resolved <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        return Math.min(resolved, MAX_SIZE);
    }

    private CustomerFilter toFilter(QueryCustomersQuery query) {
        return new CustomerFilter(
                query.name().filter(value -> !value.isBlank()),
                query.type(),
                query.document().filter(value -> !value.isBlank()));
    }
}
