package com.financialplatform.customer.features.querycustomers;

import com.financialplatform.customer.domain.Customer;
import com.financialplatform.customer.domain.CustomerNotFoundException;
import com.financialplatform.customer.ports.CustomerQueryPort;

import java.util.Objects;

public class GetCustomerByIdUseCase {

    private final CustomerQueryPort customerQuery;

    public GetCustomerByIdUseCase(CustomerQueryPort customerQuery) {
        this.customerQuery = Objects.requireNonNull(customerQuery, "CustomerQueryPort is required");
    }

    public CustomerDetailResult execute(GetCustomerByIdQuery query) {
        Objects.requireNonNull(query, "Query is required");

        Customer customer = customerQuery.findById(query.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(query.customerId().value()));

        return CustomerDetailResult.from(customer);
    }
}
