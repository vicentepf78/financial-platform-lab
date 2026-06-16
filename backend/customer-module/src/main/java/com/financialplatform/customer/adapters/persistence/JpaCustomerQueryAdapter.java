package com.financialplatform.customer.adapters.persistence;

import com.financialplatform.customer.application.readmodel.CustomerFilter;
import com.financialplatform.customer.application.readmodel.CustomerSummary;
import com.financialplatform.customer.application.readmodel.PageRequest;
import com.financialplatform.customer.application.readmodel.PageResult;
import com.financialplatform.customer.domain.Customer;
import com.financialplatform.customer.ports.CustomerQueryPort;
import com.financialplatform.sharedkernel.domain.Identifier;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public class JpaCustomerQueryAdapter implements CustomerQueryPort {

    private final CustomerJpaRepository jpaRepository;

    public JpaCustomerQueryAdapter(CustomerJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Customer> findById(Identifier id) {
        return jpaRepository.findById(id.value()).map(CustomerEntityMapper::toDomain);
    }

    @Override
    public PageResult<CustomerSummary> findAll(CustomerFilter filter, PageRequest page) {
        Specification<CustomerEntity> specification = CustomerQuerySpecifications.fromFilter(filter);
        org.springframework.data.domain.PageRequest springPage =
                org.springframework.data.domain.PageRequest.of(page.page(), page.size());

        Page<CustomerEntity> result = jpaRepository.findAll(specification, springPage);

        List<CustomerSummary> content = result.getContent().stream()
                .map(CustomerSummaryMapper::toSummary)
                .toList();

        return new PageResult<>(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }
}
