package com.financialplatform.account.support;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;

@SpringBootApplication(
        scanBasePackages = {
                "com.financialplatform.account.features",
                "com.financialplatform.account.infrastructure",
                "com.financialplatform.account.adapters.persistence",
                "com.financialplatform.account.adapters.customer",
                "com.financialplatform.account.adapters.ledger",
                "com.financialplatform.infrastructure.security",
                "com.financialplatform.features.auth"
        },
        exclude = KafkaAutoConfiguration.class)
public class AccountModuleTestApplication {
}
