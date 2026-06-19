package com.financialplatform.account.features.transfermoney;

import com.financialplatform.account.domain.Account;
import com.financialplatform.account.domain.AccountNotFoundException;
import com.financialplatform.account.domain.InsufficientBalanceException;
import com.financialplatform.account.domain.Transfer;
import com.financialplatform.account.domain.TransferDomainService;
import com.financialplatform.account.ports.AccountRepositoryPort;
import com.financialplatform.account.ports.EventPublisherPort;
import com.financialplatform.account.ports.LedgerPort;
import com.financialplatform.account.ports.TransferRepositoryPort;
import com.financialplatform.sharedkernel.domain.DomainEvent;
import com.financialplatform.sharedkernel.domain.Identifier;
import com.financialplatform.sharedkernel.domain.Money;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TransferMoneyUseCase {

    private static final String DEFAULT_ACTOR = "system";

    private final AccountRepositoryPort accountRepository;
    private final TransferRepositoryPort transferRepository;
    private final LedgerPort ledgerPort;
    private final EventPublisherPort eventPublisher;
    private final TransferDomainService transferDomainService;
    private final Clock clock;

    public TransferMoneyUseCase(
            AccountRepositoryPort accountRepository,
            TransferRepositoryPort transferRepository,
            LedgerPort ledgerPort,
            EventPublisherPort eventPublisher,
            TransferDomainService transferDomainService,
            Clock clock) {
        this.accountRepository = Objects.requireNonNull(accountRepository, "Account repository is required");
        this.transferRepository = Objects.requireNonNull(transferRepository, "Transfer repository is required");
        this.ledgerPort = Objects.requireNonNull(ledgerPort, "Ledger port is required");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "Event publisher is required");
        this.transferDomainService =
                Objects.requireNonNull(transferDomainService, "Transfer domain service is required");
        this.clock = Objects.requireNonNull(clock, "Clock is required");
    }

    public TransferMoneyResult execute(TransferMoneyCommand command) {
        Objects.requireNonNull(command, "Command is required");

        if (hasIdempotencyKey(command.idempotencyKey())) {
            return transferRepository
                    .findByIdempotencyKey(command.idempotencyKey())
                    .map(TransferMoneyResult::from)
                    .orElseGet(() -> executeTransfer(command));
        }

        return executeTransfer(command);
    }

    private TransferMoneyResult executeTransfer(TransferMoneyCommand command) {
        Identifier originId = Identifier.of(command.originAccountId());
        Identifier destinationId = Identifier.of(command.destinationAccountId());

        Account origin = accountRepository
                .findById(originId)
                .orElseThrow(() -> new AccountNotFoundException(command.originAccountId()));
        Account destination = accountRepository
                .findById(destinationId)
                .orElseThrow(() -> new AccountNotFoundException(command.destinationAccountId()));

        Money amount = Money.brl(command.amount());
        transferDomainService.validateAccountsActiveAndDistinct(origin, destination, amount);

        Money originBalance = ledgerPort.getBalanceProjection(originId);
        if (!originBalance.isGreaterThanOrEqual(amount)) {
            throw new InsufficientBalanceException(originId, amount, originBalance);
        }

        String correlationId = resolveCorrelationId(command.correlationId());
        Instant now = Instant.now(clock);
        Transfer transfer = Transfer.execute(originId, destinationId, amount, correlationId, now);
        List<DomainEvent> events = transfer.pullDomainEvents();

        ledgerPort.recordTransfer(
                transfer.id(), originId, destinationId, amount, correlationId);

        Transfer toPersist = Transfer.reconstitute(
                transfer.id(),
                transfer.originAccountId(),
                transfer.destinationAccountId(),
                transfer.amount(),
                transfer.status(),
                transfer.correlationId(),
                command.idempotencyKey(),
                resolveActor(command.actor()),
                transfer.createdAt());

        Transfer saved = transferRepository.save(toPersist);
        events.forEach(eventPublisher::publish);

        return TransferMoneyResult.from(saved);
    }

    private boolean hasIdempotencyKey(String idempotencyKey) {
        return idempotencyKey != null && !idempotencyKey.isBlank();
    }

    private String resolveCorrelationId(UUID correlationId) {
        return correlationId != null ? correlationId.toString() : UUID.randomUUID().toString();
    }

    private String resolveActor(String actor) {
        return actor == null || actor.isBlank() ? DEFAULT_ACTOR : actor;
    }
}
