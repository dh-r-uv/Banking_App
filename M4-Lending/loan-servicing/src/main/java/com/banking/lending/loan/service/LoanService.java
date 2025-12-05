package com.banking.lending.loan.service;

import com.banking.lending.loan.model.LoanApplication;
import com.banking.lending.loan.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.Map;

@Service
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${credit-engine.url}")
    private String creditEngineUrl;

    @Value("${payment-gateway.url:http://localhost:8085/api/payments}")
    private String paymentGatewayUrl;

    @Value("${business-function.url:http://localhost:8083/api/business/functions}")
    private String businessFunctionUrl;

    public LoanApplication submitLoanApplication(Long userId, BigDecimal amount, BigDecimal monthlyIncome, BigDecimal currentDebt, String targetAccount, String docs) {
        LoanApplication app = new LoanApplication();
        app.setUserId(userId);
        app.setAmount(amount);
        app.setMonthlyIncome(monthlyIncome);
        app.setCurrentDebt(currentDebt);
        app.setTargetAccount(targetAccount);
        app.setFinancialDocsJson(docs);
        app.setStatus("PENDING");
        return loanRepository.save(app);
    }

    public String runCreditCheck(Long loanId) {
        LoanApplication app = loanRepository.findById(loanId).orElseThrow(() -> new RuntimeException("Loan not found"));
        
        // Call Technical Service (Credit Engine)
        String url = creditEngineUrl + "/score?userId=" + app.getUserId() + 
                     "&monthlyIncome=" + app.getMonthlyIncome() + 
                     "&currentDebt=" + (app.getCurrentDebt() != null ? app.getCurrentDebt() : BigDecimal.ZERO);
        
        Integer score = restTemplate.getForObject(url, Integer.class);

        if (score != null && score > 700) {
            if (validateTargetAccount(app.getTargetAccount())) {
                app.setStatus("APPROVED");
                disburseLoan(app);
                generateRepaymentSchedule(app);
            } else {
                app.setStatus("REJECTED_INVALID_ACCOUNT");
            }
        } else {
            app.setStatus("REJECTED");
        }
        loanRepository.save(app);
        return app.getStatus();
    }

    private boolean validateTargetAccount(String accountNumber) {
        if (accountNumber == null || accountNumber.isEmpty()) return false;
        try {
            // Check if account exists via Business Function Service
            // Expect 200 OK if exists, 404/Exception if not
            restTemplate.getForEntity(businessFunctionUrl + "/accounts/" + accountNumber, Map.class);
            return true;
        } catch (Exception e) {
            System.err.println("Account validation failed for " + accountNumber + ": " + e.getMessage());
            return false;
        }
    }

    private void disburseLoan(LoanApplication app) {
        if (app.getTargetAccount() == null || app.getTargetAccount().isEmpty()) {
            System.err.println("Cannot disburse loan: Target account missing");
            return;
        }
        try {
            // Call Payment Gateway to deposit funds
            java.util.Map<String, Object> request = new java.util.HashMap<>();
            request.put("targetAccount", app.getTargetAccount());
            request.put("amount", app.getAmount());
            
            restTemplate.postForEntity(paymentGatewayUrl + "/deposit", request, String.class, 
                java.util.Collections.singletonMap("X-User-Id", app.getUserId()));
            
            System.out.println("Loan " + app.getId() + " disbursed to " + app.getTargetAccount());
        } catch (Exception e) {
            System.err.println("Disbursement failed: " + e.getMessage());
            // In real world: handle failure, maybe revert status or retry
        }
    }

    @Autowired
    private com.banking.lending.loan.repository.RepaymentScheduleRepository repaymentRepository;

    private void generateRepaymentSchedule(LoanApplication app) {
        // Simple Logic: 12 months, 5% flat interest
        BigDecimal interestRate = new BigDecimal("0.05");
        BigDecimal totalAmount = app.getAmount().add(app.getAmount().multiply(interestRate));
        BigDecimal monthlyPayment = totalAmount.divide(BigDecimal.valueOf(12), 2, java.math.RoundingMode.HALF_UP);

        for (int i = 1; i <= 12; i++) {
            com.banking.lending.loan.model.RepaymentSchedule schedule = new com.banking.lending.loan.model.RepaymentSchedule();
            schedule.setLoanId(app.getId());
            schedule.setAmountDue(monthlyPayment);
            schedule.setDueDate(java.time.LocalDate.now().plusMonths(i));
            schedule.setPaid(false);
            repaymentRepository.save(schedule);
        }
    }

    public String repayLoan(Long loanId, BigDecimal amount) {
        java.util.List<com.banking.lending.loan.model.RepaymentSchedule> schedules = repaymentRepository.findByLoanIdAndPaidFalseOrderByDueDateAsc(loanId);
        
        BigDecimal remainingAmount = amount;
        int paymentsMade = 0;

        for (com.banking.lending.loan.model.RepaymentSchedule schedule : schedules) {
            if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) break;

            if (remainingAmount.compareTo(schedule.getAmountDue()) >= 0) {
                // Full payment of this installment
                BigDecimal paidAmount = schedule.getAmountDue();
                schedule.setPaid(true);
                schedule.setAmountDue(BigDecimal.ZERO);
                repaymentRepository.save(schedule);
                remainingAmount = remainingAmount.subtract(paidAmount);
                paymentsMade++;
            } else {
                // Partial payment of this installment
                BigDecimal newDue = schedule.getAmountDue().subtract(remainingAmount);
                schedule.setAmountDue(newDue);
                repaymentRepository.save(schedule);
                remainingAmount = BigDecimal.ZERO;
            }
        }
        return "Processed " + paymentsMade + " full installments. Remaining balance applied to next due.";
    }

    public java.util.List<LoanApplication> getLoansByUserId(Long userId) {
        java.util.List<LoanApplication> loans = loanRepository.findByUserId(userId);
        loans.forEach(this::populateNextInstallment);
        return loans;
    }

    public java.util.List<LoanApplication> getAllLoans() {
        java.util.List<LoanApplication> loans = loanRepository.findAll();
        loans.forEach(this::populateNextInstallment);
        return loans;
    }

    private void populateNextInstallment(LoanApplication app) {
        if ("APPROVED".equals(app.getStatus())) {
            java.util.List<com.banking.lending.loan.model.RepaymentSchedule> schedules = repaymentRepository.findByLoanIdAndPaidFalseOrderByDueDateAsc(app.getId());
            if (!schedules.isEmpty()) {
                app.setNextInstallmentAmount(schedules.get(0).getAmountDue());
            } else {
                 app.setNextInstallmentAmount(BigDecimal.ZERO);
            }
        }
    }

    public java.util.List<com.banking.lending.loan.model.RepaymentSchedule> getRepaymentSchedule(Long loanId) {
        return repaymentRepository.findByLoanId(loanId);
    }
}
