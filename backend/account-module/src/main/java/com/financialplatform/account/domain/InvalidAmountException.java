package com.financialplatform.account.domain;

import com.financialplatform.sharedkernel.domain.Money;

public class InvalidAmountException extends RuntimeException {

    public InvalidAmountException(Money amount) {
        super("Transfer amount must be greater than zero: " + amount);
    }
}
