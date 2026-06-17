package com.financialplatform.account.infrastructure;

import com.financialplatform.account.features.transfermoney.TransferMoneyCommand;
import com.financialplatform.account.features.transfermoney.TransferMoneyResult;
import com.financialplatform.account.features.transfermoney.TransferMoneyUseCase;
import org.springframework.transaction.annotation.Transactional;

/**
 * Infrastructure boundary that wraps {@link TransferMoneyUseCase} with a Spring transaction so
 * transfer persistence rolls back when ledger recording or downstream persistence fails.
 */
public class TransferMoneyTransactionalBoundary {

    private final TransferMoneyUseCase delegate;

    public TransferMoneyTransactionalBoundary(TransferMoneyUseCase delegate) {
        this.delegate = delegate;
    }

    @Transactional(rollbackFor = Exception.class)
    public TransferMoneyResult execute(TransferMoneyCommand command) {
        return delegate.execute(command);
    }
}
