package com.financialplatform.account.domain;

import com.financialplatform.sharedkernel.domain.DomainEvent;
import com.financialplatform.sharedkernel.domain.Identifier;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {

    private static final Identifier CUSTOMER_ID = Identifier.of("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final Instant NOW = Instant.parse("2026-06-15T10:00:00Z");

    @Test
    void shouldOpenAccountWhenDataIsValid() {
        Account account = Account.open(CUSTOMER_ID, "system", NOW);

        assertThat(account.id()).isNotNull();
        assertThat(account.customerId()).isEqualTo(CUSTOMER_ID);
        assertThat(account.status()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(account.createdAt()).isEqualTo(NOW);
        assertThat(account.createdBy()).isEqualTo("system");
    }

    @Test
    void shouldRegisterAccountCreatedEventWhenOpened() {
        Account account = Account.open(CUSTOMER_ID, "system", NOW);

        List<DomainEvent> events = account.pullDomainEvents();

        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOf(AccountCreated.class);

        AccountCreated created = (AccountCreated) events.getFirst();
        assertThat(created.eventType()).isEqualTo("AccountCreated");
        assertThat(created.aggregateId()).isEqualTo(account.id());
        assertThat(created.customerId()).isEqualTo(CUSTOMER_ID);
        assertThat(created.status()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(created.occurredAt()).isEqualTo(NOW);
        assertThat(created.createdBy()).isEqualTo("system");
        assertThat(created.eventId()).isNotNull();
    }

    @Test
    void shouldRejectAccountWhenCustomerIdIsNull() {
        assertThatThrownBy(() -> Account.open(null, "system", NOW))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("CustomerId");
    }

    @Test
    void shouldRejectAccountWhenActorIsBlank() {
        assertThatThrownBy(() -> Account.open(CUSTOMER_ID, "  ", NOW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void shouldReconstituteAccountWithoutDomainEvents() {
        Identifier accountId = Identifier.generate();

        Account account = Account.reconstitute(accountId, CUSTOMER_ID, AccountStatus.ACTIVE, NOW, "system");

        assertThat(account.id()).isEqualTo(accountId);
        assertThat(account.pullDomainEvents()).isEmpty();
    }
}
