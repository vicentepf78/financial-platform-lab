package com.financialplatform.account.adapters.messaging;

import com.financialplatform.account.ports.EventPublisherPort;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@AutoConfigureAfter(KafkaAutoConfiguration.class)
@ConditionalOnBean(KafkaTemplate.class)
public class AccountMessagingConfig {

    @Bean
    AccountCreatedJsonSerializer accountCreatedJsonSerializer() {
        return new AccountCreatedJsonSerializer();
    }

    @Bean
    EventPublisherPort eventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            AccountCreatedJsonSerializer accountCreatedJsonSerializer) {
        return new KafkaEventPublisherAdapter(kafkaTemplate, accountCreatedJsonSerializer);
    }
}
