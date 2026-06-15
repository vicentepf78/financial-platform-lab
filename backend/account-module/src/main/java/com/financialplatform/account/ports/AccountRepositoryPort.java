package com.financialplatform.account.ports;

import com.financialplatform.account.domain.Account;
import com.financialplatform.sharedkernel.domain.Identifier;

import java.util.Optional;

public interface AccountRepositoryPort {

    Account save(Account account);

    Optional<Account> findById(Identifier id);
}
