package com.financialplatform.customer.adapters.persistence;

import com.financialplatform.customer.domain.Customer;
import com.financialplatform.customer.ports.CustomerRepositoryPort;
import com.financialplatform.sharedkernel.domain.Identifier;

import java.util.Optional;

public class JpaCustomerRepository implements CustomerRepositoryPort {

    private final CustomerJpaRepository jpaRepository;

    public JpaCustomerRepository(CustomerJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public boolean existsByDocument(String documentDigits) {
        return jpaRepository.existsByDocument(documentDigits);
    }

    @Override
    public Customer save(Customer customer) {
        CustomerEntity entity = CustomerEntityMapper.toEntity(customer);
        CustomerEntity saved = jpaRepository.save(entity);
        return CustomerEntityMapper.toDomain(saved);
    }

    @Override
    public Optional<Customer> findById(Identifier id) {
        return jpaRepository.findById(id.value()).map(CustomerEntityMapper::toDomain);
    }
}
