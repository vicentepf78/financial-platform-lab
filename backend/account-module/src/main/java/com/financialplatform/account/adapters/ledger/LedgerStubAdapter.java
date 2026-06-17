package com.financialplatform.account.adapters.ledger;

import com.financialplatform.account.domain.InsufficientBalanceException;
import com.financialplatform.account.ports.LedgerPort;
import com.financialplatform.sharedkernel.domain.Identifier;
import com.financialplatform.sharedkernel.domain.Money;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class LedgerStubAdapter implements LedgerPort {

    private final List<LedgerEntryStub> entries = new ArrayList<>();
    private final Object mutationLock = new Object();

    @Override
    public void initializeAccount(Identifier accountId) {
        Objects.requireNonNull(accountId, "AccountId is required");
    }

    @Override
    public Money getBalanceProjection(Identifier accountId) {
        Objects.requireNonNull(accountId, "AccountId is required");
        synchronized (mutationLock) {
            return projectBalance(accountId);
        }
    }

    @Override
    public void recordTransfer(
            Identifier transferId,
            Identifier originAccountId,
            Identifier destinationAccountId,
            Money amount,
            String correlationId) {
        Objects.requireNonNull(transferId, "TransferId is required");
        Objects.requireNonNull(originAccountId, "OriginAccountId is required");
        Objects.requireNonNull(destinationAccountId, "DestinationAccountId is required");
        Objects.requireNonNull(amount, "Amount is required");
        Objects.requireNonNull(correlationId, "CorrelationId is required");
        if (correlationId.isBlank()) {
            throw new IllegalArgumentException("CorrelationId must not be blank");
        }

        synchronized (mutationLock) {
            Money originBalance = projectBalance(originAccountId);
            if (!originBalance.isGreaterThanOrEqual(amount)) {
                throw new InsufficientBalanceException(originAccountId, amount, originBalance);
            }

            entries.add(new LedgerEntryStub(
                    Identifier.generate(),
                    transferId,
                    originAccountId,
                    LedgerEntryType.DEBIT,
                    amount,
                    correlationId));
            entries.add(new LedgerEntryStub(
                    Identifier.generate(),
                    transferId,
                    destinationAccountId,
                    LedgerEntryType.CREDIT,
                    amount,
                    correlationId));
        }
    }

    /**
     * Test/dev-only helper to seed account balance via a credit entry (no matching debit).
     * Not available on production {@link com.financialplatform.account.ports.LedgerPort}.
     */
    public void creditAccount(Identifier accountId, Money amount) {
        creditAccount(accountId, amount, "ledger-stub-test-seed");
    }

    /**
     * Test/dev-only helper with explicit correlation id for traceability in integration tests.
     */
    public void creditAccount(Identifier accountId, Money amount, String correlationId) {
        Objects.requireNonNull(accountId, "AccountId is required");
        Objects.requireNonNull(amount, "Amount is required");
        Objects.requireNonNull(correlationId, "CorrelationId is required");
        if (correlationId.isBlank()) {
            throw new IllegalArgumentException("CorrelationId must not be blank");
        }

        synchronized (mutationLock) {
            entries.add(new LedgerEntryStub(
                    Identifier.generate(),
                    null,
                    accountId,
                    LedgerEntryType.CREDIT,
                    amount,
                    correlationId));
        }
    }

    private Money projectBalance(Identifier accountId) {
        Money balance = Money.zero();
        for (LedgerEntryStub entry : entries) {
            if (!entry.accountId().equals(accountId)) {
                continue;
            }
            balance = switch (entry.entryType()) {
                case CREDIT -> balance.add(entry.amount());
                case DEBIT -> balance.subtract(entry.amount());
            };
        }
        return balance;
    }
}
