package com.financialplatform.customer.adapters.persistence;

import com.financialplatform.customer.domain.Customer;
import com.financialplatform.customer.domain.CustomerType;
import com.financialplatform.customer.domain.Document;
import com.financialplatform.customer.domain.Email;
import com.financialplatform.sharedkernel.domain.Identifier;

final class CustomerEntityMapper {

    private CustomerEntityMapper() {
    }

    static CustomerEntity toEntity(Customer customer) {
        return new CustomerEntity(
                customer.id().value(),
                customer.name(),
                customer.type().name(),
                customer.document().digits(),
                customer.email().value(),
                customer.createdAt(),
                customer.createdBy(),
                customer.updatedAt(),
                customer.updatedBy());
    }

    static Customer toDomain(CustomerEntity entity) {
        CustomerType type = CustomerType.valueOf(entity.getType());
        Document document = toDocument(type, entity.getDocument());

        return Customer.reconstitute(
                Identifier.of(entity.getId()),
                entity.getName(),
                type,
                document,
                Email.of(entity.getEmail()),
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy());
    }

    private static Document toDocument(CustomerType type, String digits) {
        return Document.of(type, digits);
    }
}
