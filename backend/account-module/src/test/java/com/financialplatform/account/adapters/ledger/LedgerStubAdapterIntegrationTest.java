package com.financialplatform.account.adapters.ledger;

import com.financialplatform.account.ports.LedgerPort;
import com.financialplatform.account.support.AbstractAccountWebIntegrationTest;
import com.financialplatform.account.support.LedgerTestSupport;
import com.financialplatform.sharedkernel.domain.Identifier;
import com.financialplatform.sharedkernel.domain.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class LedgerStubAdapterIntegrationTest extends AbstractAccountWebIntegrationTest {

    @Autowired
    private LedgerPort ledgerPort;

    @Test
    void shouldSeedBalanceViaCreditAccountHelper() {
        Identifier accountId = Identifier.generate();
        ledgerPort.initializeAccount(accountId);

        assertThat(ledgerPort.getBalanceProjection(accountId)).isEqualTo(Money.zero());

        LedgerTestSupport.creditAccount(ledgerPort, accountId, Money.brl("250.00"));

        assertThat(ledgerPort.getBalanceProjection(accountId)).isEqualTo(Money.brl("250.00"));
    }
}
