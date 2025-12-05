package com.example.businesstransactionservice.service;

import com.example.businesstransactionservice.model.Account;
import com.example.businesstransactionservice.model.Transaction;
import com.example.businesstransactionservice.repository.AccountRepository;
import com.example.businesstransactionservice.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class BusinessTransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public BusinessTransactionService(AccountRepository accountRepository,
            TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public void transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
        Account fromAccount = accountRepository.findByAccountNumber(fromAccountNumber)
                .orElseThrow(() -> new RuntimeException("Source Account not found: " + fromAccountNumber));
        Account toAccount = accountRepository.findByAccountNumber(toAccountNumber)
                .orElseThrow(() -> new RuntimeException("Target Account not found: " + toAccountNumber));

        BigDecimal availableBalance = fromAccount.getBalance().subtract(fromAccount.getLockedBalance());

        if (availableBalance.compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds. Available: " + availableBalance);
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setSourceAccountNumber(fromAccountNumber);
        transaction.setTargetAccountNumber(toAccountNumber);
        transaction.setAmount(amount);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setType(Transaction.TransactionType.TRANSFER);

        transactionRepository.save(transaction);
    }

    @Transactional
    public void deposit(String accountNumber, BigDecimal amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setTargetAccountNumber(accountNumber);
        transaction.setAmount(amount);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setType(Transaction.TransactionType.DEPOSIT);

        transactionRepository.save(transaction);
    }

    public Account createAccount(String ownerName, BigDecimal initialBalance, Long userId) {
        Account account = new Account();
        account.setOwnerName(ownerName);
        account.setBalance(initialBalance);
        account.setUserId(userId);
        account.setAccountNumber(UUID.randomUUID().toString());
        account.setStatus(Account.AccountStatus.ACTIVE);
        return accountRepository.save(account);
    }

    @Transactional
    public void updateAccountDetails(String accountNumber, String newOwnerName) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        account.setOwnerName(newOwnerName);
        accountRepository.save(account);
    }
}
