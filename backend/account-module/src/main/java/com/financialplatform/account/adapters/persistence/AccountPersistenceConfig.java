package com.financialplatform.account.adapters.persistence;

import com.financialplatform.account.ports.AccountRepositoryPort;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@EntityScan(basePackageClasses = AccountEntity.class)
@EnableJpaRepositories(basePackageClasses = AccountJpaRepository.class)
public class AccountPersistenceConfig {

    @Bean
    AccountRepositoryPort accountRepository(AccountJpaRepository accountJpaRepository) {
        return new JpaAccountRepository(accountJpaRepository);
    }
}
