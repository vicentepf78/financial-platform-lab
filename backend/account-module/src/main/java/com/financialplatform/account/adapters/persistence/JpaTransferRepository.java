package com.financialplatform.account.adapters.persistence;

import com.financialplatform.account.domain.Transfer;
import com.financialplatform.account.ports.TransferRepositoryPort;

import java.util.Optional;

public class JpaTransferRepository implements TransferRepositoryPort {

    private final TransferJpaRepository jpaRepository;

    public JpaTransferRepository(TransferJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Transfer save(Transfer transfer) {
        TransferEntity entity = TransferEntityMapper.toEntity(transfer);
        TransferEntity saved = jpaRepository.save(entity);
        return TransferEntityMapper.toDomain(saved);
    }

    @Override
    public Optional<Transfer> findByIdempotencyKey(String key) {
        return jpaRepository.findByIdempotencyKey(key).map(TransferEntityMapper::toDomain);
    }
}
