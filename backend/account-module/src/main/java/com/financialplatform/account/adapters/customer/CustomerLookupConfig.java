package com.financialplatform.account.adapters.customer;

import com.financialplatform.account.ports.CustomerLookupPort;
import com.financialplatform.customer.ports.CustomerRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerLookupConfig {

    @Bean
    CustomerLookupPort customerLookupPort(CustomerRepositoryPort customerRepository) {
        return new InProcessCustomerLookupAdapter(customerRepository);
    }
}
