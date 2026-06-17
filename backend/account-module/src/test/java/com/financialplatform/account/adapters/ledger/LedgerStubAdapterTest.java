package com.financialplatform.account.adapters.ledger;

import com.financialplatform.account.domain.InsufficientBalanceException;
import com.financialplatform.sharedkernel.domain.Identifier;
import com.financialplatform.sharedkernel.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LedgerStubAdapterTest {

    private static final Identifier ACCOUNT_ID = Identifier.of("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final Identifier ORIGIN_ID = Identifier.of("11111111-1111-1111-1111-111111111111");
    private static final Identifier DESTINATION_ID = Identifier.of("22222222-2222-2222-2222-222222222222");
    private static final Identifier TRANSFER_ID = Identifier.of("33333333-3333-3333-3333-333333333333");
    private static final String CORRELATION_ID = "corr-transfer-001";

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

    @Test
    void shouldReturnZeroForUninitializedAccount() {
        Money balance = adapter.getBalanceProjection(ACCOUNT_ID);

        assertThat(balance).isEqualTo(Money.zero());
    }

    @Test
    void shouldRejectNullAccountIdOnGetBalanceProjection() {
        assertThatThrownBy(() -> adapter.getBalanceProjection(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("AccountId is required");
    }

    @Test
    void shouldRejectTransferWhenInsufficientBalance() {
        adapter.initializeAccount(ORIGIN_ID);
        adapter.initializeAccount(DESTINATION_ID);
        Money amount = Money.brl("100.00");

        assertThatThrownBy(() -> adapter.recordTransfer(
                        TRANSFER_ID, ORIGIN_ID, DESTINATION_ID, amount, CORRELATION_ID))
                .isInstanceOf(InsufficientBalanceException.class);

        assertThat(adapter.getBalanceProjection(ORIGIN_ID)).isEqualTo(Money.zero());
        assertThat(adapter.getBalanceProjection(DESTINATION_ID)).isEqualTo(Money.zero());
    }

    @Test
    void shouldRecordTransferAndUpdateBalanceProjections() {
        adapter.initializeAccount(ORIGIN_ID);
        adapter.initializeAccount(DESTINATION_ID);
        adapter.creditAccount(ORIGIN_ID, Money.brl("500.00"), "seed-credit");
        Money amount = Money.brl("150.50");

        adapter.recordTransfer(TRANSFER_ID, ORIGIN_ID, DESTINATION_ID, amount, CORRELATION_ID);

        assertThat(adapter.getBalanceProjection(ORIGIN_ID)).isEqualTo(Money.brl("349.50"));
        assertThat(adapter.getBalanceProjection(DESTINATION_ID)).isEqualTo(Money.brl("150.50"));
    }

    @Test
    void shouldNotPersistEntriesWhenTransferFails() {
        adapter.initializeAccount(ORIGIN_ID);
        adapter.initializeAccount(DESTINATION_ID);
        adapter.creditAccount(ORIGIN_ID, Money.brl("50.00"), "seed-credit");
        Money amount = Money.brl("100.00");

        assertThatThrownBy(() -> adapter.recordTransfer(
                        TRANSFER_ID, ORIGIN_ID, DESTINATION_ID, amount, CORRELATION_ID))
                .isInstanceOf(InsufficientBalanceException.class);

        assertThat(adapter.getBalanceProjection(ORIGIN_ID)).isEqualTo(Money.brl("50.00"));
        assertThat(adapter.getBalanceProjection(DESTINATION_ID)).isEqualTo(Money.zero());
    }

    @Test
    void shouldRejectNullTransferIdOnRecordTransfer() {
        assertThatThrownBy(() -> adapter.recordTransfer(
                        null, ORIGIN_ID, DESTINATION_ID, Money.brl("10.00"), CORRELATION_ID))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("TransferId is required");
    }

    @Test
    void shouldRejectBlankCorrelationIdOnRecordTransfer() {
        assertThatThrownBy(() -> adapter.recordTransfer(
                        TRANSFER_ID, ORIGIN_ID, DESTINATION_ID, Money.brl("10.00"), "  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CorrelationId must not be blank");
    }
}
