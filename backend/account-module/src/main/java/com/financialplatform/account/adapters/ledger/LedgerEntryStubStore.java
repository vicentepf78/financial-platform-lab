package com.financialplatform.account.adapters.ledger;

import com.financialplatform.sharedkernel.domain.Identifier;
import com.financialplatform.sharedkernel.domain.Money;

public interface LedgerEntryStubStore {

    Money getBalanceProjection(Identifier accountId);

    default void lockAccountForTransfer(Identifier accountId) {
    }

    void recordTransfer(
            Identifier transferId,
            Identifier originAccountId,
            Identifier destinationAccountId,
            Money amount,
            String correlationId);

    void creditAccount(Identifier accountId, Money amount, String correlationId);
}
