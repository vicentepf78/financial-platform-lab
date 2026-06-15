package com.financialplatform.account.adapters.persistence;

import com.financialplatform.account.domain.Account;
import com.financialplatform.account.domain.AccountStatus;
import com.financialplatform.account.ports.AccountRepositoryPort;
import com.financialplatform.account.support.AbstractAccountIntegrationTest;
import com.financialplatform.sharedkernel.domain.Identifier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class JpaAccountRepositoryIntegrationTest extends AbstractAccountIntegrationTest {

    private static final Instant NOW = Instant.parse("2026-06-15T10:00:00Z");

    @Autowired
    private AccountRepositoryPort repository;

    @Test
    void shouldPersistAccountWhenCustomerExists() {
        Identifier customerId = seedCustomer();
        Account account = Account.open(customerId, "system", NOW);

        Account saved = repository.save(account);

        assertThat(saved.id()).isEqualTo(account.id());
        assertThat(saved.customerId()).isEqualTo(customerId);
        assertThat(saved.status()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(saved.createdAt()).isEqualTo(NOW);
        assertThat(saved.createdBy()).isEqualTo("system");
    }

    @Test
    void shouldFindAccountByIdWhenAccountExists() {
        Identifier customerId = seedCustomer();
        Account account = Account.open(customerId, "system", NOW);
        repository.save(account);

        assertThat(repository.findById(account.id()))
                .isPresent()
                .get()
                .satisfies(found -> {
                    assertThat(found.id()).isEqualTo(account.id());
                    assertThat(found.customerId()).isEqualTo(customerId);
                    assertThat(found.status()).isEqualTo(AccountStatus.ACTIVE);
                });
    }
}
