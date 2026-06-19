package com.financialplatform.account.adapters.persistence;

import com.financialplatform.account.adapters.ledger.LedgerEntryStubStore;
import com.financialplatform.account.adapters.ledger.LedgerEntryType;
import com.financialplatform.sharedkernel.domain.Identifier;
import com.financialplatform.sharedkernel.domain.Money;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class JpaLedgerEntryStubStore implements LedgerEntryStubStore {

    private final LedgerEntryStubJpaRepository repository;
    private final EntityManager entityManager;

    public JpaLedgerEntryStubStore(LedgerEntryStubJpaRepository repository, EntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    @Override
    public Money getBalanceProjection(Identifier accountId) {
        BigDecimal balance = repository.projectBalance(accountId.value());
        return Money.brl(balance);
    }

    @Override
    public void lockAccountForTransfer(Identifier accountId) {
        entityManager
                .createNativeQuery("SELECT pg_advisory_xact_lock(hashtext(?1))")
                .setParameter(1, accountId.value().toString())
                .getSingleResult();
    }

    @Override
    public void recordTransfer(
            Identifier transferId,
            Identifier originAccountId,
            Identifier destinationAccountId,
            Money amount,
            String correlationId) {
        UUID correlationUuid = toUuid(correlationId);
        Instant now = Instant.now();
        repository.saveAll(List.of(
                entry(transferId, originAccountId, LedgerEntryType.DEBIT, amount, correlationUuid, now),
                entry(transferId, destinationAccountId, LedgerEntryType.CREDIT, amount, correlationUuid, now)));
    }

    @Override
    public void creditAccount(Identifier accountId, Money amount, String correlationId) {
        repository.save(new LedgerEntryStubEntity(
                Identifier.generate().value(),
                null,
                accountId.value(),
                LedgerEntryType.CREDIT.name(),
                amount.amount(),
                amount.currency().getCurrencyCode(),
                toUuid(correlationId),
                Instant.now()));
    }

    private static LedgerEntryStubEntity entry(
            Identifier transferId,
            Identifier accountId,
            LedgerEntryType entryType,
            Money amount,
            UUID correlationId,
            Instant createdAt) {
        return new LedgerEntryStubEntity(
                Identifier.generate().value(),
                transferId.value(),
                accountId.value(),
                entryType.name(),
                amount.amount(),
                amount.currency().getCurrencyCode(),
                correlationId,
                createdAt);
    }

    private static UUID toUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            return UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8));
        }
    }
}
