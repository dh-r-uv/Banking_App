package com.banking.lending.credit.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Random;

@Service
public class CreditService {

    /**
     * Calculates a credit score based on user ID and financial inputs.
     * In a real system, this would use Python/ML models.
     * Here we interpret "optimized for data science" as "logic isolated here".
     */
    public int calculateCreditScore(Long userId, BigDecimal monthlyIncome, BigDecimal currentDebt) {
        // Mock logic: Score between 300 and 850
        // Base score 600 + (Income / 100) capped at 850
        int baseScore = 600;
        int incomeFactor = monthlyIncome.divide(BigDecimal.valueOf(100)).intValue();
        
        // DTI Penalty
        if (monthlyIncome.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal dti = currentDebt.divide(monthlyIncome, 2, java.math.RoundingMode.HALF_UP);
            if (dti.compareTo(new BigDecimal("0.40")) > 0) {
                // High DTI (>40%) reduces score significantly
                baseScore -= 100;
            } else if (dti.compareTo(new BigDecimal("0.20")) < 0) {
                 // Low DTI (<20%) boosts score
                 baseScore += 50;
            }
        }

        int score = baseScore + incomeFactor;
        return Math.min(score, 850);
    }

    /**
     * Validates collateral.
     */
    public boolean validateCollateral(String collateralType, BigDecimal value) {
        // Mock logic: Real estate and Vehicle > 5000 are valid
        if (value.compareTo(BigDecimal.valueOf(5000)) < 0) {
            return false;
        }
        return "REAL_ESTATE".equalsIgnoreCase(collateralType) || "VEHICLE".equalsIgnoreCase(collateralType);
    }
}
