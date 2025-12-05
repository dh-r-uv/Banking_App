package com.example.coreaccounts.service;

import com.example.coreaccounts.model.Account;
import com.example.coreaccounts.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TechnicalFunctionService {

    private final AccountRepository accountRepository;

    public TechnicalFunctionService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public String verifyAccountStatus(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return account.getStatus().name();
    }

    @Transactional
    public void lockAccountFunds(String accountNumber, BigDecimal amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        // Logic to lock funds (e.g., reduce available balance or set a flag)
        // For simplicity, we'll just log it or assume it checks balance
        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds to lock");
        }
        // In a real system, we might have a 'lockedAmount' field.
        System.out.println("Funds locked for account: " + accountNumber + ", Amount: " + amount);
    }
}
