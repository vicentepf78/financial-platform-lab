package com.financialplatform.account.adapters.persistence;

import com.financialplatform.account.domain.Account;
import com.financialplatform.account.domain.Transfer;
import com.financialplatform.account.domain.TransferStatus;
import com.financialplatform.account.ports.TransferRepositoryPort;
import com.financialplatform.account.support.AbstractAccountIntegrationTest;
import com.financialplatform.sharedkernel.domain.Identifier;
import com.financialplatform.sharedkernel.domain.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class JpaTransferRepositoryIntegrationTest extends AbstractAccountIntegrationTest {

    private static final Instant NOW = Instant.parse("2026-06-15T14:00:00Z");
    private static final String CORRELATION_ID = "770e8400-e29b-41d4-a716-446655440002";

    @Autowired
    private TransferRepositoryPort repository;

    @Test
    void shouldPersistTransferWhenAccountsExist() {
        Identifier customerId = seedCustomer();
        Account origin = seedAccount(customerId);
        Account destination = seedAccount(customerId);
        Transfer transfer = sampleTransfer(origin.id(), destination.id(), "client-req-001");

        Transfer saved = repository.save(transfer);

        assertThat(saved.id()).isEqualTo(transfer.id());
        assertThat(saved.originAccountId()).isEqualTo(origin.id());
        assertThat(saved.destinationAccountId()).isEqualTo(destination.id());
        assertThat(saved.amount()).isEqualTo(Money.brl("250.50"));
        assertThat(saved.status()).isEqualTo(TransferStatus.COMPLETED);
        assertThat(saved.correlationId()).isEqualTo(CORRELATION_ID);
        assertThat(saved.idempotencyKey()).isEqualTo("client-req-001");
        assertThat(saved.createdBy()).isEqualTo("system");
        assertThat(saved.createdAt()).isEqualTo(NOW);
    }

    @Test
    void shouldFindTransferByIdempotencyKeyWhenTransferExists() {
        Identifier customerId = seedCustomer();
        Account origin = seedAccount(customerId);
        Account destination = seedAccount(customerId);
        Transfer transfer = sampleTransfer(origin.id(), destination.id(), "client-req-002");
        repository.save(transfer);

        assertThat(repository.findByIdempotencyKey("client-req-002"))
                .isPresent()
                .get()
                .satisfies(found -> {
                    assertThat(found.id()).isEqualTo(transfer.id());
                    assertThat(found.originAccountId()).isEqualTo(origin.id());
                    assertThat(found.destinationAccountId()).isEqualTo(destination.id());
                    assertThat(found.amount()).isEqualTo(Money.brl("250.50"));
                    assertThat(found.idempotencyKey()).isEqualTo("client-req-002");
                });
    }

    @Test
    void shouldReturnEmptyWhenIdempotencyKeyDoesNotExist() {
        assertThat(repository.findByIdempotencyKey("missing-key")).isEmpty();
    }

    @Test
    void shouldPersistTransferWithoutIdempotencyKey() {
        Identifier customerId = seedCustomer();
        Account origin = seedAccount(customerId);
        Account destination = seedAccount(customerId);
        Identifier transferId = Identifier.generate();
        Transfer transfer = Transfer.reconstitute(
                transferId,
                origin.id(),
                destination.id(),
                Money.brl("100.00"),
                TransferStatus.COMPLETED,
                CORRELATION_ID,
                null,
                "operator",
                NOW);

        Transfer saved = repository.save(transfer);

        assertThat(saved.id()).isEqualTo(transferId);
        assertThat(saved.idempotencyKey()).isNull();
        assertThat(saved.createdBy()).isEqualTo("operator");
        assertThat(repository.findByIdempotencyKey("client-req-001")).isEmpty();
    }

    private static Transfer sampleTransfer(
            Identifier originAccountId, Identifier destinationAccountId, String idempotencyKey) {
        return Transfer.reconstitute(
                Identifier.generate(),
                originAccountId,
                destinationAccountId,
                Money.brl("250.50"),
                TransferStatus.COMPLETED,
                CORRELATION_ID,
                idempotencyKey,
                "system",
                NOW);
    }
}
