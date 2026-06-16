package com.financialplatform.customer.adapters.persistence;

import com.financialplatform.customer.application.readmodel.CustomerSummary;
import com.financialplatform.customer.domain.CustomerType;
import com.financialplatform.customer.domain.Document;

final class CustomerSummaryMapper {

    private CustomerSummaryMapper() {
    }

    static CustomerSummary toSummary(CustomerEntity entity) {
        CustomerType type = CustomerType.valueOf(entity.getType());
        Document document = Document.of(type, entity.getDocument());

        return new CustomerSummary(
                entity.getId(),
                entity.getName(),
                type,
                document.formatted(),
                entity.getEmail(),
                entity.getCreatedAt());
    }
}
