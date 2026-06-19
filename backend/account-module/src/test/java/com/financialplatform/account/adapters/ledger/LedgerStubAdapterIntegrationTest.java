package com.financialplatform.account.adapters.ledger;

import com.financialplatform.account.domain.Account;
import com.financialplatform.account.ports.LedgerPort;
import com.financialplatform.account.support.AbstractAccountWebIntegrationTest;
import com.financialplatform.account.support.LedgerTestSupport;
import com.financialplatform.sharedkernel.domain.Identifier;
import com.financialplatform.sharedkernel.domain.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class LedgerStubAdapterIntegrationTest extends AbstractAccountWebIntegrationTest {

    @Autowired
    private LedgerPort ledgerPort;

    @Test
    void shouldSeedBalanceViaCreditAccountHelper() {
        Identifier customerId = seedCustomer();
        Identifier accountId = seedAccount(customerId).id();
        ledgerPort.initializeAccount(accountId);

        assertThat(ledgerPort.getBalanceProjection(accountId)).isEqualTo(Money.zero());

        LedgerTestSupport.creditAccount(ledgerPort, accountId, Money.brl("250.00"));

        assertThat(ledgerPort.getBalanceProjection(accountId)).isEqualTo(Money.brl("250.00"));
        assertThat(countLedgerEntries()).isEqualTo(1);
    }

    @Test
    void shouldPersistDebitAndCreditEntriesOnRecordTransfer() {
        Identifier customerId = seedCustomer();
        Account origin = seedAccount(customerId);
        Account destination = seedAccount(customerId);
        Identifier originId = origin.id();
        Identifier destinationId = destination.id();
        Identifier transferId = Identifier.generate();
        seedTransfer(transferId, originId, destinationId);
        LedgerTestSupport.creditAccount(ledgerPort, originId, Money.brl("250.00"));

        ledgerPort.recordTransfer(
                transferId, originId, destinationId, Money.brl("75.00"), "770e8400-e29b-41d4-a716-446655440002");

        assertThat(ledgerPort.getBalanceProjection(originId)).isEqualTo(Money.brl("175.00"));
        assertThat(ledgerPort.getBalanceProjection(destinationId)).isEqualTo(Money.brl("75.00"));
        assertThat(countLedgerEntriesForTransfer(transferId)).isEqualTo(2);
    }

    private void seedTransfer(Identifier transferId, Identifier originId, Identifier destinationId) {
        jdbcTemplate.update(
                """
                INSERT INTO transfers (
                    id, origin_account_id, destination_account_id, amount, currency, status,
                    correlation_id, idempotency_key, created_at, created_by
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                transferId.value(),
                originId.value(),
                destinationId.value(),
                new BigDecimal("75.00"),
                "BRL",
                "COMPLETED",
                java.util.UUID.fromString("770e8400-e29b-41d4-a716-446655440002"),
                null,
                Timestamp.from(Instant.parse("2026-06-15T14:00:00Z")),
                "operator");
    }

    private int countLedgerEntries() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ledger_entries_stub", Integer.class);
        return count == null ? 0 : count;
    }

    private int countLedgerEntriesForTransfer(Identifier transferId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ledger_entries_stub WHERE transfer_id = ?",
                Integer.class,
                transferId.value());
        return count == null ? 0 : count;
    }
}
