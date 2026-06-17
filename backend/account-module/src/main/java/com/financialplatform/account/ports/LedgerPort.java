package com.financialplatform.account.ports;

import com.financialplatform.sharedkernel.domain.Identifier;
import com.financialplatform.sharedkernel.domain.Money;

public interface LedgerPort {

    void initializeAccount(Identifier accountId);

    Money getBalanceProjection(Identifier accountId);

    /**
     * Records double-entry ledger lines: debit origin, credit destination.
     * Must be atomic.
     */
    void recordTransfer(
            Identifier transferId,
            Identifier originAccountId,
            Identifier destinationAccountId,
            Money amount,
            String correlationId);
}
