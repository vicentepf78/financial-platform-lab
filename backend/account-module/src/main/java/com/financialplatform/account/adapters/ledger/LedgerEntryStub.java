package com.financialplatform.account.adapters.ledger;

import com.financialplatform.sharedkernel.domain.Identifier;
import com.financialplatform.sharedkernel.domain.Money;

record LedgerEntryStub(
        Identifier id,
        Identifier transferId,
        Identifier accountId,
        LedgerEntryType entryType,
        Money amount,
        String correlationId) {
}
