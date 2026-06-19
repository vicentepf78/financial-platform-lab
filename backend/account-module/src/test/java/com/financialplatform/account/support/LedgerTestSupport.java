package com.financialplatform.account.support;

import com.financialplatform.account.adapters.ledger.LedgerStubAdapter;
import com.financialplatform.account.ports.LedgerPort;
import com.financialplatform.sharedkernel.domain.Identifier;
import com.financialplatform.sharedkernel.domain.Money;

/**
 * Test/dev helpers for seeding ledger balances when {@link LedgerStubAdapter} is the active port.
 */
public final class LedgerTestSupport {

    private LedgerTestSupport() {
    }

    /**
     * Seeds balance on the stub ledger. Only supported when the Spring context wires
     * {@link LedgerStubAdapter} (local/test/dev); production ledger adapters reject this call.
     */
    public static void creditAccount(LedgerPort ledgerPort, Identifier accountId, Money amount) {
        requireStub(ledgerPort).creditAccount(accountId, amount);
    }

    private static LedgerStubAdapter requireStub(LedgerPort ledgerPort) {
        if (ledgerPort instanceof LedgerStubAdapter stub) {
            return stub;
        }
        throw new IllegalStateException(
                "creditAccount is only supported with LedgerStubAdapter (test/dev environments)");
    }
}
