package com.financialplatform.customer.infrastructure;

import com.financialplatform.customer.adapters.persistence.CustomerPersistenceConfig;
import com.financialplatform.customer.features.createcustomer.CreateCustomerUseCase;
import com.financialplatform.customer.features.querycustomers.GetCustomerByIdUseCase;
import com.financialplatform.customer.features.querycustomers.QueryCustomersUseCase;
import com.financialplatform.customer.features.updatecustomer.UpdateCustomerUseCase;
import com.financialplatform.customer.ports.CustomerQueryPort;
import com.financialplatform.customer.ports.CustomerRepositoryPort;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.time.Clock;

@Configuration
@ConditionalOnProperty(prefix = "spring.datasource", name = "url")
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@Import(CustomerPersistenceConfig.class)
public class CustomerModuleConfig {

    @Bean
    Clock customerModuleClock() {
        return Clock.systemUTC();
    }

    @Bean
    CreateCustomerUseCase createCustomerUseCase(
            CustomerRepositoryPort customerRepository,
            Clock customerModuleClock) {
        return new CreateCustomerUseCase(customerRepository, customerModuleClock);
    }

    @Bean
    QueryCustomersUseCase queryCustomersUseCase(CustomerQueryPort customerQueryPort) {
        return new QueryCustomersUseCase(customerQueryPort);
    }

    @Bean
    GetCustomerByIdUseCase getCustomerByIdUseCase(CustomerQueryPort customerQueryPort) {
        return new GetCustomerByIdUseCase(customerQueryPort);
    }

    @Bean
    UpdateCustomerUseCase updateCustomerUseCase(
            CustomerRepositoryPort customerRepository,
            Clock customerModuleClock) {
        return new UpdateCustomerUseCase(customerRepository, customerModuleClock);
    }
}
