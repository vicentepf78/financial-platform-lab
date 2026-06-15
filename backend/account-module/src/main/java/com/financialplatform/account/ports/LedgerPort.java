package com.financialplatform.account.ports;

import com.financialplatform.sharedkernel.domain.Identifier;
import com.financialplatform.sharedkernel.domain.Money;

public interface LedgerPort {

    void initializeAccount(Identifier accountId);

    Money getBalanceProjection(Identifier accountId);
}
