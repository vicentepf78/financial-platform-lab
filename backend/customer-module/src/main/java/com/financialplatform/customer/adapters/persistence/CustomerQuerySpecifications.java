package com.financialplatform.customer.adapters.persistence;

import com.financialplatform.customer.application.readmodel.CustomerFilter;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

final class CustomerQuerySpecifications {

    private CustomerQuerySpecifications() {
    }

    static Specification<CustomerEntity> fromFilter(CustomerFilter filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            filter.name().ifPresent(name -> {
                String pattern = "%" + name.toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        pattern));
            });

            filter.type().ifPresent(type ->
                    predicates.add(criteriaBuilder.equal(root.get("type"), type.name())));

            filter.documentDigits().ifPresent(documentRaw -> {
                String digits = documentRaw.replaceAll("\\D", "");
                predicates.add(criteriaBuilder.equal(root.get("document"), digits));
            });

            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
