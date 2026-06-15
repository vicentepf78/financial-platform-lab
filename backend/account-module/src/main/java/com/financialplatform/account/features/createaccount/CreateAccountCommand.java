package com.financialplatform.account.features.createaccount;

import java.util.UUID;

public record CreateAccountCommand(
        UUID customerId,
        String actor) {
}
