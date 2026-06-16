package com.financialplatform;

import com.financialplatform.customer.features.createcustomer.CreateCustomerUseCase;
import com.financialplatform.customer.features.querycustomers.GetCustomerByIdUseCase;
import com.financialplatform.customer.features.querycustomers.QueryCustomersUseCase;
import com.financialplatform.customer.infrastructure.CustomerExceptionHandler;
import com.financialplatform.customer.ports.CustomerRepositoryPort;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

@SpringBootTest(
        classes = FinancialPlatformApplicationTest.SmokeTestApplication.class,
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
class FinancialPlatformApplicationTest {

    @MockBean
    private CustomerRepositoryPort customerRepositoryPort;

    @MockBean
    private CreateCustomerUseCase createCustomerUseCase;

    @MockBean
    private QueryCustomersUseCase queryCustomersUseCase;

    @MockBean
    private GetCustomerByIdUseCase getCustomerByIdUseCase;

    @Test
    void shouldLoadApplicationContext() {
        // context load verification
    }

    @SpringBootApplication(scanBasePackages = {
            "com.financialplatform.infrastructure",
            "com.financialplatform.customer.features"
    })
    @Import(CustomerExceptionHandler.class)
    static class SmokeTestApplication {
    }
}
