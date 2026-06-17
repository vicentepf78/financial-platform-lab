package com.financialplatform.account.domain;

import com.financialplatform.sharedkernel.domain.Identifier;
import com.financialplatform.sharedkernel.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransferDomainServiceTest {

    private static final Identifier CUSTOMER_ID = Identifier.of("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final Identifier ORIGIN_ID = Identifier.of("550e8400-e29b-41d4-a716-446655440000");
    private static final Identifier DESTINATION_ID = Identifier.of("660e8400-e29b-41d4-a716-446655440001");
    private static final Instant NOW = Instant.parse("2026-06-15T14:00:00Z");
    private static final Money AMOUNT = Money.brl("100.00");

    private TransferDomainService domainService;

    @BeforeEach
    void setUp() {
        domainService = new TransferDomainService();
    }

    @Test
    void shouldValidateWhenAccountsAreActiveAndDistinct() {
        Account origin = account(ORIGIN_ID, AccountStatus.ACTIVE);
        Account destination = account(DESTINATION_ID, AccountStatus.ACTIVE);

        assertThatCode(() -> domainService.validateAccountsActiveAndDistinct(origin, destination, AMOUNT))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectWhenSameAccount() {
        Account account = account(ORIGIN_ID, AccountStatus.ACTIVE);

        assertThatThrownBy(() -> domainService.validateAccountsActiveAndDistinct(account, account, AMOUNT))
                .isInstanceOf(SameAccountTransferException.class)
                .hasMessageContaining(ORIGIN_ID.toString());
    }

    @Test
    void shouldRejectWhenOriginIsInactive() {
        Account origin = account(ORIGIN_ID, AccountStatus.CLOSED);
        Account destination = account(DESTINATION_ID, AccountStatus.ACTIVE);

        assertThatThrownBy(() -> domainService.validateAccountsActiveAndDistinct(origin, destination, AMOUNT))
                .isInstanceOf(InactiveAccountException.class)
                .hasMessageContaining(ORIGIN_ID.toString())
                .hasMessageContaining("CLOSED");
    }

    @Test
    void shouldRejectWhenDestinationIsInactive() {
        Account origin = account(ORIGIN_ID, AccountStatus.ACTIVE);
        Account destination = account(DESTINATION_ID, AccountStatus.SUSPENDED);

        assertThatThrownBy(() -> domainService.validateAccountsActiveAndDistinct(origin, destination, AMOUNT))
                .isInstanceOf(InactiveAccountException.class)
                .hasMessageContaining(DESTINATION_ID.toString())
                .hasMessageContaining("SUSPENDED");
    }

    private static Account account(Identifier id, AccountStatus status) {
        return Account.reconstitute(id, CUSTOMER_ID, status, NOW, "system");
    }
}
