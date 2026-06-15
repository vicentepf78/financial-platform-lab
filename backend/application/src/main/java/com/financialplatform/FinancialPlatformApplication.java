package com.financialplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.financialplatform")
public class FinancialPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinancialPlatformApplication.class, args);
    }
}
