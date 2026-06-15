package com.financialplatform.account.adapters.ledger;

import com.financialplatform.account.ports.LedgerPort;
import com.financialplatform.sharedkernel.domain.Identifier;
import com.financialplatform.sharedkernel.domain.Money;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class LedgerStubAdapter implements LedgerPort {

    private final Set<Identifier> initializedAccounts = ConcurrentHashMap.newKeySet();

    @Override
    public void initializeAccount(Identifier accountId) {
        Objects.requireNonNull(accountId, "AccountId is required");
        initializedAccounts.add(accountId);
    }

    @Override
    public Money getBalanceProjection(Identifier accountId) {
        Objects.requireNonNull(accountId, "AccountId is required");
        return Money.zero();
    }
}
