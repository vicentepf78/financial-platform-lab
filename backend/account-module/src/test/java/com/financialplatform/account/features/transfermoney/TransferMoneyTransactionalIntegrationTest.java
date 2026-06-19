package com.financialplatform.account.features.transfermoney;

import com.financialplatform.account.domain.Account;
import com.financialplatform.account.infrastructure.TransferMoneyTransactionalBoundary;
import com.financialplatform.account.ports.LedgerPort;
import com.financialplatform.account.support.AbstractAccountWebIntegrationTest;
import com.financialplatform.sharedkernel.domain.Identifier;
import com.financialplatform.sharedkernel.domain.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransferMoneyTransactionalIntegrationTest extends AbstractAccountWebIntegrationTest {

    @Autowired
    private TransferMoneyTransactionalBoundary transferMoneyTransactionalBoundary;

    @Autowired
    private LedgerPort ledgerPort;

    @Test
    void shouldNotPersistTransferWhenInsufficientBalancePreventsLedgerWrite() {
        Identifier customerId = seedCustomer();
        Account origin = seedAccount(customerId);
        Account destination = seedAccount(customerId);

        TransferMoneyCommand command = new TransferMoneyCommand(
                origin.id().value(),
                destination.id().value(),
                new BigDecimal("100.00"),
                null,
                null,
                null);

        assertThatThrownBy(() -> transferMoneyTransactionalBoundary.execute(command))
                .hasMessageContaining("Insufficient balance");

        assertThat(countTransfers()).isZero();
        assertThat(countLedgerEntries()).isZero();
        assertThat(ledgerPort.getBalanceProjection(origin.id())).isEqualTo(Money.zero());
    }

    private int countTransfers() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM transfers", Integer.class);
        return count == null ? 0 : count;
    }

    private int countLedgerEntries() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ledger_entries_stub", Integer.class);
        return count == null ? 0 : count;
    }
}
