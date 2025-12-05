package com.example.technicalservice.service;

import com.example.technicalservice.model.Account;
import com.example.technicalservice.repository.AccountRepository;
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

        BigDecimal availableBalance = account.getBalance().subtract(account.getLockedBalance());

        if (availableBalance.compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds to lock. Available: " + availableBalance);
        }

        account.setLockedBalance(account.getLockedBalance().add(amount));
        accountRepository.save(account);

        System.out.println(
                "Locked " + amount + " in account " + accountNumber + ". Total Locked: " + account.getLockedBalance());
    }
}
