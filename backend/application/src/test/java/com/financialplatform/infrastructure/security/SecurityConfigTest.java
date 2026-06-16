package com.financialplatform.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = SecurityConfigTest.TestApplication.class,
        properties = {
                "spring.autoconfigure.exclude="
                        + "com.financialplatform.customer.infrastructure.CustomerModuleConfig,"
                        + "com.financialplatform.account.infrastructure.AccountModuleConfig,"
                        + "com.financialplatform.account.adapters.messaging.AccountMessagingConfig"
        })
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        FlywayAutoConfiguration.class,
        KafkaAutoConfiguration.class
})
@TestPropertySource(properties = {
        "security.jwt.enabled=false",
        "security.jwt.secret=test-secret",
        "security.jwt.expiration-seconds=7200"
})
class SecurityConfigTest {

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Autowired
    private JwtProperties jwtProperties;

    @Test
    void shouldLoadSecurityFilterChainWhenJwtDisabled() {
        assertThat(securityFilterChain).isNotNull();
    }

    @Test
    void shouldBindJwtProperties() {
        assertThat(jwtProperties.isEnabled()).isFalse();
        assertThat(jwtProperties.getSecret()).isEqualTo("test-secret");
        assertThat(jwtProperties.getExpirationSeconds()).isEqualTo(7200);
        assertThat(jwtProperties.getUsers()).containsKeys("operator", "admin");
        assertThat(jwtProperties.getUsers().get("operator").getPassword()).isEqualTo("operator");
        assertThat(jwtProperties.getUsers().get("operator").getRoles()).containsExactly("OPERATOR");
        assertThat(jwtProperties.getUsers().get("admin").getPassword()).isEqualTo("admin");
        assertThat(jwtProperties.getUsers().get("admin").getRoles())
                .containsExactlyInAnyOrder("ADMIN", "OPERATOR");
    }

    @SpringBootApplication
    @Import(SecurityConfig.class)
    static class TestApplication {
    }
}
