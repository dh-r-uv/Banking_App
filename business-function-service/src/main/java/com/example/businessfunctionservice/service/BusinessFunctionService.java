package com.example.businessfunctionservice.service;

import com.example.businessfunctionservice.model.Account;
import com.example.businessfunctionservice.model.Transaction;
import com.example.businessfunctionservice.repository.AccountRepository;
import com.example.businessfunctionservice.repository.TransactionRepository;
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
        BigDecimal balance = getAccountBalance(accountNumber);
        List<Transaction> transactions = viewTransactionHistory(accountNumber);
        return "Statement for " + accountNumber + ": Balance=" + balance + ", Transactions=" + transactions.size();
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Account getAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }
}
