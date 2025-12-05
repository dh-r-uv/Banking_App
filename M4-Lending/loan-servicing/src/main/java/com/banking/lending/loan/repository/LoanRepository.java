package com.banking.lending.loan.repository;

import com.banking.lending.loan.model.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<LoanApplication, Long> {
    java.util.List<LoanApplication> findByUserId(Long userId);
}
