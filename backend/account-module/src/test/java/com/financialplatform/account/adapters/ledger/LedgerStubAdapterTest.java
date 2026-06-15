package com.financialplatform.account.adapters.ledger;

import com.financialplatform.sharedkernel.domain.Identifier;
import com.financialplatform.sharedkernel.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class LedgerStubAdapterTest {

    private static final Identifier ACCOUNT_ID = Identifier.of("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    private LedgerStubAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new LedgerStubAdapter();
    }

    @Test
    void shouldReturnZeroBalanceProjectionForNewAccount() {
        adapter.initializeAccount(ACCOUNT_ID);

        Money balance = adapter.getBalanceProjection(ACCOUNT_ID);

        assertThat(balance).isEqualTo(Money.zero());
    }

    @Test
    void shouldInitializeAccountIdempotently() {
        adapter.initializeAccount(ACCOUNT_ID);

        assertThatCode(() -> adapter.initializeAccount(ACCOUNT_ID))
                .doesNotThrowAnyException();
        assertThat(adapter.getBalanceProjection(ACCOUNT_ID)).isEqualTo(Money.zero());
    }
}
