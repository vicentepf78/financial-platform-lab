package com.financialplatform.account.support;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;

@SpringBootApplication(
        scanBasePackages = "com.financialplatform.account.adapters.persistence",
        exclude = KafkaAutoConfiguration.class)
public class AccountPersistenceTestApplication {
}
