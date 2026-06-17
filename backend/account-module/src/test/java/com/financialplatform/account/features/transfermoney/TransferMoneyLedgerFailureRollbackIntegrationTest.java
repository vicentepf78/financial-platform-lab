package com.financialplatform.account.features.transfermoney;

import com.financialplatform.account.domain.Account;
import com.financialplatform.account.infrastructure.TransferMoneyTransactionalBoundary;
import com.financialplatform.account.ports.LedgerPort;
import com.financialplatform.account.support.AbstractAccountWebIntegrationTest;
import com.financialplatform.sharedkernel.domain.Identifier;
import com.financialplatform.sharedkernel.domain.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TransferMoneyLedgerFailureRollbackIntegrationTest.FailingLedgerConfig.class)
class TransferMoneyLedgerFailureRollbackIntegrationTest extends AbstractAccountWebIntegrationTest {

    @Autowired
    private TransferMoneyTransactionalBoundary transferMoneyTransactionalBoundary;

    @Test
    void shouldRollbackTransferPersistenceWhenLedgerRecordTransferFails() {
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
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ledger write failed");

        assertThat(countTransfers()).isZero();
    }

    private int countTransfers() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM transfers", Integer.class);
        return count == null ? 0 : count;
    }

    @TestConfiguration
    static class FailingLedgerConfig {

        @Bean
        @Primary
        LedgerPort failingLedgerPort() {
            return new LedgerPort() {
                @Override
                public void initializeAccount(Identifier accountId) {
                }

                @Override
                public Money getBalanceProjection(Identifier accountId) {
                    return Money.brl("500.00");
                }

                @Override
                public void recordTransfer(
                        Identifier transferId,
                        Identifier originAccountId,
                        Identifier destinationAccountId,
                        Money amount,
                        String correlationId) {
                    throw new IllegalStateException("ledger write failed");
                }
            };
        }
    }
}
