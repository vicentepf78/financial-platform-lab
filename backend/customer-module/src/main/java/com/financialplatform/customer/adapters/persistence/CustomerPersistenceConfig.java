package com.financialplatform.customer.adapters.persistence;

import com.financialplatform.customer.ports.CustomerRepositoryPort;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@EntityScan(basePackageClasses = CustomerEntity.class)
@EnableJpaRepositories(basePackageClasses = CustomerJpaRepository.class)
public class CustomerPersistenceConfig {

    @Bean
    CustomerRepositoryPort customerRepository(CustomerJpaRepository customerJpaRepository) {
        return new JpaCustomerRepository(customerJpaRepository);
    }
}
