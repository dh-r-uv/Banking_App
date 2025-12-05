package com.banking.lending.credit.controller;

import com.banking.lending.credit.service.CreditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/credit")
public class CreditController {

    @Autowired
    private CreditService creditService;

    @GetMapping("/score")
    public int getCreditScore(@RequestParam Long userId, @RequestParam BigDecimal monthlyIncome, @RequestParam(defaultValue = "0") BigDecimal currentDebt) {
        return creditService.calculateCreditScore(userId, monthlyIncome, currentDebt);
    }

    @PostMapping("/validate-collateral")
    public boolean validateCollateral(@RequestBody Map<String, Object> payload) {
        String type = (String) payload.get("type");
        BigDecimal value = new BigDecimal(payload.get("value").toString());
        return creditService.validateCollateral(type, value);
    }
}
