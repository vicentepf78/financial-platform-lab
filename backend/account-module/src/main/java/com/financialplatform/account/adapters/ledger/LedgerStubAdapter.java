package com.financialplatform.account.adapters.ledger;

import com.financialplatform.account.ports.LedgerPort;
import com.financialplatform.sharedkernel.domain.Identifier;
import com.financialplatform.sharedkernel.domain.Money;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class LedgerStubAdapter implements LedgerPort {

    private final Map<Identifier, Money> balanceProjections = new ConcurrentHashMap<>();

    @Override
    public void initializeAccount(Identifier accountId) {
        Objects.requireNonNull(accountId, "AccountId is required");
        balanceProjections.putIfAbsent(accountId, Money.zero());
    }

    @Override
    public Money getBalanceProjection(Identifier accountId) {
        Objects.requireNonNull(accountId, "AccountId is required");
        return balanceProjections.getOrDefault(accountId, Money.zero());
    }
}
