package com.financialplatform.account.adapters.messaging;

import com.financialplatform.account.ports.EventPublisherPort;
import com.financialplatform.sharedkernel.domain.DomainEvent;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Objects;

public class AfterCommitEventPublisherAdapter implements EventPublisherPort {

    private final EventPublisherPort delegate;

    public AfterCommitEventPublisherAdapter(EventPublisherPort delegate) {
        this.delegate = Objects.requireNonNull(delegate, "Event publisher delegate is required");
    }

    @Override
    public void publish(DomainEvent event) {
        Objects.requireNonNull(event, "Event is required");
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            delegate.publish(event);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                delegate.publish(event);
            }
        });
    }
}
