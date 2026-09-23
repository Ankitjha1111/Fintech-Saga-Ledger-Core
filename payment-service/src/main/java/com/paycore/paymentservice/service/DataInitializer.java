package com.paycore.paymentservice.service;

import com.paycore.paymentservice.model.Account;
import com.paycore.paymentservice.repository.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(AccountRepository accountRepository) {
        return args -> {
            // Check if accounts already exist
            if (accountRepository.findByAccountNumber("ACC-CUST-001").isEmpty()) {
                Account customerAccount = new Account();
                customerAccount.setAccountNumber("ACC-CUST-001");
                customerAccount.setAccountHolder("CUSTOMER_WALLET");
                customerAccount.setBalance(new BigDecimal("10000.00"));
                accountRepository.save(customerAccount);
            }

            if (accountRepository.findByAccountNumber("ACC-MERCH-001").isEmpty()) {
                Account merchantAccount = new Account();
                merchantAccount.setAccountNumber("ACC-MERCH-001");
                merchantAccount.setAccountHolder("MERCHANT_POOL");
                merchantAccount.setBalance(new BigDecimal("0.00"));
                accountRepository.save(merchantAccount);
            }

            System.out.println(">> Sample Accounts Initialized Successfully!");
        };
    }
}