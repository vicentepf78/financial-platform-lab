package com.financialplatform.account.domain;

import com.financialplatform.sharedkernel.domain.Identifier;
import com.financialplatform.sharedkernel.domain.Money;

public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(Identifier accountId, Money requested, Money available) {
        super("Insufficient balance on account "
                + accountId.value()
                + ": requested "
                + requested
                + ", available "
                + available);
    }
}
