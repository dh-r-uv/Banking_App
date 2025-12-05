package com.example.businesstransactionservice.controller;

import com.example.businesstransactionservice.model.Account;
import com.example.businesstransactionservice.service.BusinessTransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/business/transactions")
public class BusinessTransactionController {

    private final BusinessTransactionService businessTransactionService;

    public BusinessTransactionController(BusinessTransactionService businessTransactionService) {
        this.businessTransactionService = businessTransactionService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@RequestBody Map<String, Object> request) {
        String fromAccount = (String) request.get("fromAccount");
        String toAccount = (String) request.get("toAccount");
        BigDecimal amount = new BigDecimal(request.get("amount").toString());

        businessTransactionService.transfer(fromAccount, toAccount, amount);
        return ResponseEntity.ok("Transfer successful");
    }

    @PostMapping("/deposit")
    public ResponseEntity<String> deposit(@RequestBody Map<String, Object> request) {
        String accountNumber = (String) request.get("accountNumber");
        BigDecimal amount = new BigDecimal(request.get("amount").toString());

        businessTransactionService.deposit(accountNumber, amount);
        return ResponseEntity.ok("Deposit successful");
    }

    @PostMapping("/accounts")
    public ResponseEntity<Account> createAccount(@RequestBody Map<String, Object> request) {
        String ownerName = (String) request.get("ownerName");
        BigDecimal initialBalance = new BigDecimal(request.get("initialBalance").toString());
        Long userId = Long.valueOf(request.get("userId").toString());

        return ResponseEntity.ok(businessTransactionService.createAccount(ownerName, initialBalance, userId));
    }

    @PutMapping("/accounts/{accountNumber}")
    public ResponseEntity<String> updateAccountDetails(@PathVariable String accountNumber,
            @RequestBody Map<String, String> request) {
        businessTransactionService.updateAccountDetails(accountNumber, request.get("ownerName"));
        return ResponseEntity.ok("Account details updated");
    }
}
