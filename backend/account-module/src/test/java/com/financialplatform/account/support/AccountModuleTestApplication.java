package com.financialplatform.account.support;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration(exclude = KafkaAutoConfiguration.class)
@ComponentScan(basePackages = {
        "com.financialplatform.account.features",
        "com.financialplatform.account.infrastructure",
        "com.financialplatform.account.adapters.persistence",
        "com.financialplatform.account.adapters.customer",
        "com.financialplatform.account.adapters.ledger"
})
public class AccountModuleTestApplication {
}
