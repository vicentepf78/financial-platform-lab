package com.financialplatform.account.domain;

import com.financialplatform.sharedkernel.domain.DomainEvent;
import com.financialplatform.sharedkernel.domain.Identifier;
import com.financialplatform.sharedkernel.domain.Money;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransferTest {

    private static final Identifier ORIGIN_ID = Identifier.of("550e8400-e29b-41d4-a716-446655440000");
    private static final Identifier DESTINATION_ID = Identifier.of("660e8400-e29b-41d4-a716-446655440001");
    private static final Instant NOW = Instant.parse("2026-06-15T14:00:00Z");
    private static final String CORRELATION_ID = "770e8400-e29b-41d4-a716-446655440002";

    @Test
    void shouldExecuteTransferWhenDataIsValid() {
        Money amount = Money.brl("250.50");

        Transfer transfer = Transfer.execute(ORIGIN_ID, DESTINATION_ID, amount, CORRELATION_ID, NOW);

        assertThat(transfer.id()).isNotNull();
        assertThat(transfer.originAccountId()).isEqualTo(ORIGIN_ID);
        assertThat(transfer.destinationAccountId()).isEqualTo(DESTINATION_ID);
        assertThat(transfer.amount()).isEqualTo(amount);
        assertThat(transfer.status()).isEqualTo(TransferStatus.COMPLETED);
        assertThat(transfer.correlationId()).isEqualTo(CORRELATION_ID);
        assertThat(transfer.createdAt()).isEqualTo(NOW);
    }

    @Test
    void shouldRegisterTransferExecutedEventWhenExecuted() {
        Money amount = Money.brl("250.50");

        Transfer transfer = Transfer.execute(ORIGIN_ID, DESTINATION_ID, amount, CORRELATION_ID, NOW);

        List<DomainEvent> events = transfer.pullDomainEvents();

        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOf(TransferExecuted.class);

        TransferExecuted executed = (TransferExecuted) events.getFirst();
        assertThat(executed.eventType()).isEqualTo("TransferExecuted");
        assertThat(executed.aggregateId()).isEqualTo(transfer.id());
        assertThat(executed.originAccountId()).isEqualTo(ORIGIN_ID);
        assertThat(executed.destinationAccountId()).isEqualTo(DESTINATION_ID);
        assertThat(executed.amount()).isEqualByComparingTo(amount.amount());
        assertThat(executed.currency()).isEqualTo("BRL");
        assertThat(executed.correlationId()).isEqualTo(CORRELATION_ID);
        assertThat(executed.occurredAt()).isEqualTo(NOW);
        assertThat(executed.eventId()).isNotNull();
    }

    @Test
    void shouldRejectTransferWhenAmountIsZero() {
        Money zero = Money.zero();

        assertThatThrownBy(() -> Transfer.execute(ORIGIN_ID, DESTINATION_ID, zero, CORRELATION_ID, NOW))
                .isInstanceOf(InvalidAmountException.class)
                .hasMessageContaining("greater than zero");
    }

    @Test
    void shouldRejectTransferWhenCorrelationIdIsBlank() {
        assertThatThrownBy(() -> Transfer.execute(ORIGIN_ID, DESTINATION_ID, Money.brl("10.00"), "  ", NOW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void shouldReconstituteTransferWithoutDomainEvents() {
        Identifier transferId = Identifier.generate();
        Money amount = Money.brl("100.00");

        Transfer transfer = Transfer.reconstitute(
                transferId, ORIGIN_ID, DESTINATION_ID, amount, TransferStatus.COMPLETED, CORRELATION_ID, NOW);

        assertThat(transfer.id()).isEqualTo(transferId);
        assertThat(transfer.pullDomainEvents()).isEmpty();
    }
}
