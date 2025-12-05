package com.example.coreaccounts.service;

import com.example.coreaccounts.model.Account;
import com.example.coreaccounts.model.Transaction;
import com.example.coreaccounts.repository.AccountRepository;
import com.example.coreaccounts.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BusinessFunctionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public BusinessFunctionService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public BigDecimal getAccountBalance(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return account.getBalance();
    }

    public List<Transaction> viewTransactionHistory(String accountNumber) {
        return transactionRepository.findBySourceAccountNumberOrTargetAccountNumber(accountNumber, accountNumber);
    }

    public String generateAccountStatement(String accountNumber) {
        // Mock statement generation
        return "Statement for " + accountNumber + ": Balance = " + getAccountBalance(accountNumber);
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Account getAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }
}
