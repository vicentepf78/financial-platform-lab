package com.financialplatform.account.infrastructure;

import com.financialplatform.account.adapters.customer.CustomerLookupConfig;
import com.financialplatform.account.adapters.ledger.LedgerStubAdapter;
import com.financialplatform.account.adapters.persistence.AccountPersistenceConfig;
import com.financialplatform.account.features.createaccount.CreateAccountUseCase;
import com.financialplatform.account.ports.AccountRepositoryPort;
import com.financialplatform.account.ports.CustomerLookupPort;
import com.financialplatform.account.ports.EventPublisherPort;
import com.financialplatform.account.ports.LedgerPort;
import com.financialplatform.customer.adapters.persistence.CustomerPersistenceConfig;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.time.Clock;

@Configuration
@ConditionalOnProperty(prefix = "spring.datasource", name = "url")
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@Import({
    AccountPersistenceConfig.class,
    CustomerPersistenceConfig.class,
    CustomerLookupConfig.class
})
public class AccountModuleConfig {

    @Bean
    Clock accountModuleClock() {
        return Clock.systemUTC();
    }

    @Bean
    LedgerPort ledgerPort() {
        return new LedgerStubAdapter();
    }

    @Bean
    @ConditionalOnMissingBean(EventPublisherPort.class)
    EventPublisherPort noOpEventPublisher() {
        return event -> {};
    }

    @Bean
    CreateAccountUseCase createAccountUseCase(
            AccountRepositoryPort accountRepository,
            LedgerPort ledgerPort,
            CustomerLookupPort customerLookup,
            EventPublisherPort eventPublisher,
            Clock accountModuleClock) {
        return new CreateAccountUseCase(
                accountRepository, ledgerPort, customerLookup, eventPublisher, accountModuleClock);
    }
}
