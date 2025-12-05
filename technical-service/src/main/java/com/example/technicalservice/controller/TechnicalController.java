package com.example.technicalservice.controller;

import com.example.technicalservice.service.TechnicalFunctionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/technical")
public class TechnicalController {

    private final TechnicalFunctionService technicalFunctionService;

    public TechnicalController(TechnicalFunctionService technicalFunctionService) {
        this.technicalFunctionService = technicalFunctionService;
    }

    @GetMapping("/{accountNumber}/status")
    public ResponseEntity<String> verifyAccountStatus(@PathVariable String accountNumber) {
        return ResponseEntity.ok(technicalFunctionService.verifyAccountStatus(accountNumber));
    }

    @PostMapping("/{accountNumber}/lock")
    public ResponseEntity<String> lockAccountFunds(@PathVariable String accountNumber,
            @RequestBody Map<String, BigDecimal> request) {
        technicalFunctionService.lockAccountFunds(accountNumber, request.get("amount"));
        return ResponseEntity.ok("Funds locked");
    }
}
