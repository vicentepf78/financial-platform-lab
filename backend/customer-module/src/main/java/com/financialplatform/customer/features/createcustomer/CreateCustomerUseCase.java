package com.financialplatform.customer.features.createcustomer;

import com.financialplatform.customer.domain.Customer;
import com.financialplatform.customer.domain.CustomerType;
import com.financialplatform.customer.domain.DuplicateDocumentException;
import com.financialplatform.customer.ports.CustomerRepositoryPort;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class CreateCustomerUseCase {

    private static final String DEFAULT_ACTOR = "system";

    private final CustomerRepositoryPort customerRepository;
    private final Clock clock;

    public CreateCustomerUseCase(CustomerRepositoryPort customerRepository, Clock clock) {
        this.customerRepository = Objects.requireNonNull(customerRepository, "Repository is required");
        this.clock = Objects.requireNonNull(clock, "Clock is required");
    }

    public CreateCustomerResult execute(CreateCustomerCommand command) {
        Objects.requireNonNull(command, "Command is required");

        Customer customer = Customer.create(
                command.name(),
                command.type(),
                command.document(),
                command.email(),
                resolveActor(command.actor()),
                Instant.now(clock));

        if (customerRepository.existsByDocument(customer.document().digits())) {
            throw new DuplicateDocumentException(customer.document().formatted());
        }

        Customer saved = customerRepository.save(customer);
        return CreateCustomerResult.from(saved);
    }

    private String resolveActor(String actor) {
        return actor == null || actor.isBlank() ? DEFAULT_ACTOR : actor;
    }
}
