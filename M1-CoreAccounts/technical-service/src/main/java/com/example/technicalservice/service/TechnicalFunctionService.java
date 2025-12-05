package com.example.technicalservice.service;

import com.example.technicalservice.model.Account;
import com.example.technicalservice.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Service
@Slf4j
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

        // System.out.println(
        // "Locked " + amount + " in account " + accountNumber + ". Total Locked: " +
        // account.getLockedBalance());
        log.info("[BANKING-CORE] Action Triggered: Locked amount={} in account={}. Total Locked={}", amount,
                accountNumber,
                account.getLockedBalance());
    }

    @Transactional
    public void unlockAccountFunds(String accountNumber, BigDecimal amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (account.getLockedBalance().compareTo(amount) < 0) {
            throw new RuntimeException(
                    "Cannot unlock " + amount + ". Only " + account.getLockedBalance() + " is locked.");
        }

        account.setLockedBalance(account.getLockedBalance().subtract(amount));
        accountRepository.save(account);

        // System.out.println(
        // "Unlocked " + amount + " in account " + accountNumber + ". Remaining Locked:
        // "
        // + account.getLockedBalance());
        log.info("[BANKING-CORE] Action Triggered: Unlocked amount={} in account={}. Remaining Locked={}", amount,
                accountNumber,
                account.getLockedBalance());
    }
}
