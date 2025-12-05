package com.banking.lending.loan.controller;

import com.banking.lending.loan.model.LoanApplication;
import com.banking.lending.loan.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/loan")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @PostMapping("/apply")
    public LoanApplication applyForLoan(@RequestBody Map<String, Object> payload) {
        Long userId = Long.valueOf(payload.get("userId").toString());
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());
        BigDecimal income = new BigDecimal(payload.get("monthlyIncome").toString());
        BigDecimal currentDebt = payload.containsKey("currentDebt") ? new BigDecimal(payload.get("currentDebt").toString()) : BigDecimal.ZERO;
        String  targetAccount = (String) payload.get("targetAccount");
        String docs = (String) payload.get("docs");
        return loanService.submitLoanApplication(userId, amount, income, currentDebt, targetAccount, docs);
    }

    @PostMapping("/{id}/check")
    public String checkCredit(@PathVariable Long id) {
        return loanService.runCreditCheck(id);
    }

    @PostMapping("/{id}/repay")
    public String repayLoan(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());
        return loanService.repayLoan(id, amount);
    }

    @GetMapping("/my-loans")
    public java.util.List<LoanApplication> getMyLoans(@RequestParam Long userId) {
        return loanService.getLoansByUserId(userId);
    }

    @GetMapping("/all")
    public java.util.List<LoanApplication> getAllLoans() {
        return loanService.getAllLoans();
    }

    @GetMapping("/{id}/schedule")
    public java.util.List<com.banking.lending.loan.model.RepaymentSchedule> getRepaymentSchedule(@PathVariable Long id) {
        return loanService.getRepaymentSchedule(id);
    }
}
