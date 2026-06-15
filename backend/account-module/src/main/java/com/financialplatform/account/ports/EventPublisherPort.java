package com.financialplatform.account.ports;

import com.financialplatform.sharedkernel.domain.DomainEvent;

public interface EventPublisherPort {

    void publish(DomainEvent event);
}
