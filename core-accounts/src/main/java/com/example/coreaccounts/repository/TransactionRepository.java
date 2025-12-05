package com.example.coreaccounts.repository;

import com.example.coreaccounts.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findBySourceAccountNumberOrTargetAccountNumber(String sourceAccountNumber, String targetAccountNumber);
}
