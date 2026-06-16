package com.financialplatform.customer.support;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;

@SpringBootApplication(
        scanBasePackages = {
                "com.financialplatform.customer",
                "com.financialplatform.infrastructure.security",
                "com.financialplatform.features.auth"
        },
        exclude = KafkaAutoConfiguration.class)
public class CustomerModuleTestApplication {
}
