package com.banking.lending.loan.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Data
public class LoanApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private BigDecimal amount;
    private BigDecimal monthlyIncome;
    private BigDecimal currentDebt; // Added for DTI check
    private String targetAccount;   // Added for disbursement
    private String status; // PENDING, APPROVED, REJECTED

    @Transient
    private BigDecimal nextInstallmentAmount;

    // Replacing NoSQL requirement by storing unstructured data here
    @Column(columnDefinition = "TEXT")
    private String financialDocsJson; // JSON string of docs
}
