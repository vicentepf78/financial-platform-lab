package com.financialplatform.account.adapters.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface TransferJpaRepository extends JpaRepository<TransferEntity, UUID> {

    Optional<TransferEntity> findByIdempotencyKey(String idempotencyKey);
}
