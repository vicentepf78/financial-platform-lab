package com.financialplatform.account.adapters.ledger;

import com.financialplatform.sharedkernel.domain.Identifier;
import com.financialplatform.sharedkernel.domain.Money;

import java.util.ArrayList;
import java.util.List;

final class InMemoryLedgerEntryStubStore implements LedgerEntryStubStore {

    private final List<LedgerEntryStub> entries = new ArrayList<>();

    @Override
    public Money getBalanceProjection(Identifier accountId) {
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

    @Override
    public void recordTransfer(
            Identifier transferId,
            Identifier originAccountId,
            Identifier destinationAccountId,
            Money amount,
            String correlationId) {
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

    @Override
    public void creditAccount(Identifier accountId, Money amount, String correlationId) {
        entries.add(new LedgerEntryStub(
                Identifier.generate(),
                null,
                accountId,
                LedgerEntryType.CREDIT,
                amount,
                correlationId));
    }
}
