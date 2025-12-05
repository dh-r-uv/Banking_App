package com.example.businessfunctionservice.controller;

import com.example.businessfunctionservice.model.Account;
import com.example.businessfunctionservice.model.Transaction;
import com.example.businessfunctionservice.service.BusinessFunctionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/business/functions")
public class BusinessFunctionController {

    private final BusinessFunctionService businessFunctionService;

    public BusinessFunctionController(BusinessFunctionService businessFunctionService) {
        this.businessFunctionService = businessFunctionService;
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<BigDecimal> getAccountBalance(@PathVariable String accountNumber) {
        return ResponseEntity.ok(businessFunctionService.getAccountBalance(accountNumber));
    }

    @GetMapping("/{accountNumber}/transactions")
    public ResponseEntity<List<Transaction>> getHistory(@PathVariable String accountNumber) {
        return ResponseEntity.ok(businessFunctionService.viewTransactionHistory(accountNumber));
    }

    @GetMapping("/{accountNumber}/statement")
    public ResponseEntity<String> generateStatement(@PathVariable String accountNumber) {
        return ResponseEntity.ok(businessFunctionService.generateAccountStatement(accountNumber));
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<Account>> getAllAccounts() {
        return ResponseEntity.ok(businessFunctionService.getAllAccounts());
    }

    @GetMapping("/accounts/{accountNumber}")
    public ResponseEntity<Account> getAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(businessFunctionService.getAccount(accountNumber));
    }
}
