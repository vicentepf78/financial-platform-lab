package com.financialplatform.account.adapters.persistence;

import com.financialplatform.account.domain.Account;
import com.financialplatform.account.domain.AccountStatus;
import com.financialplatform.sharedkernel.domain.Identifier;

final class AccountEntityMapper {

    private AccountEntityMapper() {
    }

    static AccountEntity toEntity(Account account) {
        return new AccountEntity(
                account.id().value(),
                account.customerId().value(),
                account.createdAt(),
                account.createdBy(),
                account.createdAt(),
                account.createdBy());
    }

    static Account toDomain(AccountEntity entity) {
        return Account.reconstitute(
                Identifier.of(entity.getId()),
                Identifier.of(entity.getCustomerId()),
                AccountStatus.ACTIVE,
                entity.getCreatedAt(),
                entity.getCreatedBy());
    }
}
