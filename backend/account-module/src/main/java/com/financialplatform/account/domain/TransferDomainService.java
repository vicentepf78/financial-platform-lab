package com.financialplatform.account.domain;

import com.financialplatform.sharedkernel.domain.Money;

import java.util.Objects;

public final class TransferDomainService {

    public void validateAccountsActiveAndDistinct(Account origin, Account destination, Money amount) {
        Objects.requireNonNull(origin, "Origin account is required");
        Objects.requireNonNull(destination, "Destination account is required");
        Objects.requireNonNull(amount, "Amount is required");

        if (origin.id().equals(destination.id())) {
            throw new SameAccountTransferException(origin.id());
        }
        if (!origin.isActive()) {
            throw new InactiveAccountException(origin.id(), origin.status());
        }
        if (!destination.isActive()) {
            throw new InactiveAccountException(destination.id(), destination.status());
        }
    }
}
