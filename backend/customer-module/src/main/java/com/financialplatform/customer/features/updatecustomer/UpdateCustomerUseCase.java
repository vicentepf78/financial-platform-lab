package com.financialplatform.customer.features.updatecustomer;

import com.financialplatform.customer.domain.Customer;
import com.financialplatform.customer.domain.CustomerNotFoundException;
import com.financialplatform.customer.ports.CustomerRepositoryPort;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class UpdateCustomerUseCase {

    private static final String DEFAULT_ACTOR = "system";

    private final CustomerRepositoryPort customerRepository;
    private final Clock clock;

    public UpdateCustomerUseCase(CustomerRepositoryPort customerRepository, Clock clock) {
        this.customerRepository = Objects.requireNonNull(customerRepository, "Repository is required");
        this.clock = Objects.requireNonNull(clock, "Clock is required");
    }

    public UpdateCustomerResult execute(UpdateCustomerCommand command) {
        Objects.requireNonNull(command, "Command is required");

        Customer customer = customerRepository.findById(command.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(command.customerId().value()));

        Customer updated = customer.update(
                command.name(),
                command.email(),
                null,
                null,
                resolveActor(command.actor()),
                Instant.now(clock));

        Customer saved = customerRepository.save(updated);
        return UpdateCustomerResult.from(saved);
    }

    private String resolveActor(String actor) {
        return actor == null || actor.isBlank() ? DEFAULT_ACTOR : actor;
    }
}
