package com.financialplatform.account.adapters.messaging;

import com.financialplatform.account.domain.AccountCreated;
import com.financialplatform.account.domain.AccountStatus;
import com.financialplatform.account.ports.EventPublisherPort;
import com.financialplatform.sharedkernel.domain.DomainEvent;
import com.financialplatform.sharedkernel.domain.Identifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class AfterCommitEventPublisherAdapterTest {

    private final EventPublisherPort delegate = mock(EventPublisherPort.class);
    private final AfterCommitEventPublisherAdapter adapter = new AfterCommitEventPublisherAdapter(delegate);

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void shouldPublishImmediatelyWhenNoTransactionIsActive() {
        DomainEvent event = event();

        adapter.publish(event);

        verify(delegate).publish(event);
    }

    @Test
    void shouldPublishOnlyAfterCommitWhenTransactionIsActive() {
        DomainEvent event = event();
        TransactionSynchronizationManager.initSynchronization();

        adapter.publish(event);

        verify(delegate, never()).publish(event);
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(TransactionSynchronization::afterCommit);
        verify(delegate).publish(event);
    }

    @Test
    void shouldNotPublishWhenTransactionRollsBack() {
        DomainEvent event = event();
        TransactionSynchronizationManager.initSynchronization();

        adapter.publish(event);

        TransactionSynchronizationManager.getSynchronizations()
                .forEach(synchronization -> synchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
        verify(delegate, never()).publish(event);
    }

    private static DomainEvent event() {
        return new AccountCreated(
                UUID.randomUUID(),
                Identifier.generate(),
                Identifier.generate(),
                AccountStatus.ACTIVE,
                Instant.parse("2026-06-15T10:00:00Z"),
                "operator");
    }
}
