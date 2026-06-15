package com.financialplatform.account.features.createaccount;

import com.financialplatform.account.domain.Account;
import com.financialplatform.account.domain.CustomerNotFoundException;
import com.financialplatform.account.domain.LedgerInitializationException;
import com.financialplatform.account.ports.AccountRepositoryPort;
import com.financialplatform.account.ports.CustomerLookupPort;
import com.financialplatform.account.ports.EventPublisherPort;
import com.financialplatform.account.ports.LedgerPort;
import com.financialplatform.sharedkernel.domain.DomainEvent;
import com.financialplatform.sharedkernel.domain.Identifier;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class CreateAccountUseCase {

    private static final String DEFAULT_ACTOR = "system";

    private final AccountRepositoryPort accountRepository;
    private final LedgerPort ledgerPort;
    private final CustomerLookupPort customerLookup;
    private final EventPublisherPort eventPublisher;
    private final Clock clock;

    public CreateAccountUseCase(
            AccountRepositoryPort accountRepository,
            LedgerPort ledgerPort,
            CustomerLookupPort customerLookup,
            EventPublisherPort eventPublisher,
            Clock clock) {
        this.accountRepository = Objects.requireNonNull(accountRepository, "Account repository is required");
        this.ledgerPort = Objects.requireNonNull(ledgerPort, "Ledger port is required");
        this.customerLookup = Objects.requireNonNull(customerLookup, "Customer lookup is required");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "Event publisher is required");
        this.clock = Objects.requireNonNull(clock, "Clock is required");
    }

    public CreateAccountResult execute(CreateAccountCommand command) {
        Objects.requireNonNull(command, "Command is required");

        Identifier customerId = Identifier.of(command.customerId());

        if (!customerLookup.exists(customerId)) {
            throw new CustomerNotFoundException(command.customerId());
        }

        Account account = Account.open(customerId, resolveActor(command.actor()), Instant.now(clock));

        try {
            ledgerPort.initializeAccount(account.id());
        } catch (RuntimeException exception) {
            throw new LedgerInitializationException(account.id().value(), exception);
        }

        List<DomainEvent> events = account.pullDomainEvents();
        Account saved = accountRepository.save(account);
        events.forEach(eventPublisher::publish);

        return CreateAccountResult.from(saved);
    }

    private String resolveActor(String actor) {
        return actor == null || actor.isBlank() ? DEFAULT_ACTOR : actor;
    }
}
