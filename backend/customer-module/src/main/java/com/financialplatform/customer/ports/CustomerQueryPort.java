package com.financialplatform.customer.ports;

import com.financialplatform.customer.application.readmodel.CustomerFilter;
import com.financialplatform.customer.application.readmodel.CustomerSummary;
import com.financialplatform.customer.application.readmodel.PageRequest;
import com.financialplatform.customer.application.readmodel.PageResult;
import com.financialplatform.customer.domain.Customer;
import com.financialplatform.sharedkernel.domain.Identifier;

import java.util.Optional;

public interface CustomerQueryPort {

    Optional<Customer> findById(Identifier id);

    PageResult<CustomerSummary> findAll(CustomerFilter filter, PageRequest page);
}
