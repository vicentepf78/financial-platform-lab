package com.financialplatform.account.adapters.persistence;

import com.financialplatform.account.domain.Account;
import com.financialplatform.account.ports.AccountRepositoryPort;
import com.financialplatform.sharedkernel.domain.Identifier;

import java.util.Optional;

public class JpaAccountRepository implements AccountRepositoryPort {

    private final AccountJpaRepository jpaRepository;

    public JpaAccountRepository(AccountJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Account save(Account account) {
        AccountEntity entity = AccountEntityMapper.toEntity(account);
        AccountEntity saved = jpaRepository.save(entity);
        return AccountEntityMapper.toDomain(saved);
    }

    @Override
    public Optional<Account> findById(Identifier id) {
        return jpaRepository.findById(id.value()).map(AccountEntityMapper::toDomain);
    }
}
