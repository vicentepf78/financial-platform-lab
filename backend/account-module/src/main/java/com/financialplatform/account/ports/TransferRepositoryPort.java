package com.financialplatform.account.ports;

import com.financialplatform.account.domain.Transfer;

import java.util.Optional;

public interface TransferRepositoryPort {

    Transfer save(Transfer transfer);

    Optional<Transfer> findByIdempotencyKey(String key);
}
