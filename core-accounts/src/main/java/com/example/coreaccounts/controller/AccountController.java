package com.example.coreaccounts.controller;

import com.example.coreaccounts.model.Account;
import com.example.coreaccounts.model.Transaction;
import com.example.coreaccounts.service.BusinessFunctionService;
import com.example.coreaccounts.service.BusinessTransactionService;
import com.example.coreaccounts.service.TechnicalFunctionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final TechnicalFunctionService technicalFunctionService;
    private final BusinessFunctionService businessFunctionService;
    private final BusinessTransactionService businessTransactionService;
    private final com.example.coreaccounts.repository.UserRepository userRepository;

    public AccountController(TechnicalFunctionService technicalFunctionService,
            BusinessFunctionService businessFunctionService,
            BusinessTransactionService businessTransactionService,
            com.example.coreaccounts.repository.UserRepository userRepository) {
        this.technicalFunctionService = technicalFunctionService;
        this.businessFunctionService = businessFunctionService;
        this.businessTransactionService = businessTransactionService;
        this.userRepository = userRepository;
    }

    private void checkPermission(Long userId, String accountNumber,
            com.example.coreaccounts.model.User.Role... allowedRoles) {
        com.example.coreaccounts.model.User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean roleMatch = false;
        for (com.example.coreaccounts.model.User.Role role : allowedRoles) {
            if (user.getRole() == role) {
                roleMatch = true;
                break;
            }
        }
        if (!roleMatch) {
            throw new RuntimeException("Access Denied: Insufficient Role");
        }

        if (user.getRole() == com.example.coreaccounts.model.User.Role.CUSTOMER) {
            if (accountNumber != null) {
                Account account = businessFunctionService.getAccount(accountNumber);
                if (!account.getUserId().equals(userId)) {
                    throw new RuntimeException("Access Denied: Not Account Owner");
                }
            }
        }
    }

    // --- Business Transaction Services ---

    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestHeader("X-User-Id") Long userId,
            @RequestBody Map<String, Object> request) {
        checkPermission(userId, null, com.example.coreaccounts.model.User.Role.ADMIN);
        String ownerName = (String) request.get("ownerName");
        BigDecimal initialBalance = new BigDecimal(request.get("initialBalance").toString());
        Long targetUserId = Long.valueOf(request.get("userId").toString());
        return ResponseEntity.ok(businessTransactionService.createAccount(ownerName, initialBalance, targetUserId));
    }

    @GetMapping
    public ResponseEntity<List<Account>> getAllAccounts(@RequestHeader("X-User-Id") Long userId) {
        checkPermission(userId, null, com.example.coreaccounts.model.User.Role.ADMIN);
        return ResponseEntity.ok(businessFunctionService.getAllAccounts());
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<Account> getAccount(@RequestHeader("X-User-Id") Long userId,
            @PathVariable String accountNumber) {
        checkPermission(userId, accountNumber, com.example.coreaccounts.model.User.Role.ADMIN,
                com.example.coreaccounts.model.User.Role.CUSTOMER);
        return ResponseEntity.ok(businessFunctionService.getAccount(accountNumber));
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@RequestHeader("X-User-Id") Long userId,
            @RequestBody Map<String, Object> request) {
        String fromAccount = (String) request.get("fromAccount");
        checkPermission(userId, fromAccount, com.example.coreaccounts.model.User.Role.CUSTOMER);

        String toAccount = (String) request.get("toAccount");
        BigDecimal amount = new BigDecimal(request.get("amount").toString());

        businessTransactionService.transfer(fromAccount, toAccount, amount);
        return ResponseEntity.ok("Transfer successful");
    }

    @PutMapping("/{accountNumber}")
    public ResponseEntity<String> updateAccountDetails(@RequestHeader("X-User-Id") Long userId,
            @PathVariable String accountNumber,
            @RequestBody Map<String, String> request) {
        checkPermission(userId, accountNumber, com.example.coreaccounts.model.User.Role.ADMIN);
        businessTransactionService.updateAccountDetails(accountNumber, request.get("ownerName"));
        return ResponseEntity.ok("Account details updated");
    }

    @PostMapping("/{accountNumber}/beneficiaries")
    public ResponseEntity<String> manageBeneficiaries(@RequestHeader("X-User-Id") Long userId,
            @PathVariable String accountNumber,
            @RequestBody Map<String, String> request) {
        checkPermission(userId, accountNumber, com.example.coreaccounts.model.User.Role.ADMIN);
        businessTransactionService.manageBeneficiaries(accountNumber, request.get("beneficiaryName"));
        return ResponseEntity.ok("Beneficiary added");
    }

    // --- Business Function Services ---

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<BigDecimal> getAccountBalance(@RequestHeader("X-User-Id") Long userId,
            @PathVariable String accountNumber) {
        checkPermission(userId, accountNumber, com.example.coreaccounts.model.User.Role.ADMIN,
                com.example.coreaccounts.model.User.Role.CUSTOMER);
        return ResponseEntity.ok(businessFunctionService.getAccountBalance(accountNumber));
    }

    @GetMapping("/{accountNumber}/transactions")
    public ResponseEntity<List<Transaction>> getHistory(@RequestHeader("X-User-Id") Long userId,
            @PathVariable String accountNumber) {
        checkPermission(userId, accountNumber, com.example.coreaccounts.model.User.Role.ADMIN,
                com.example.coreaccounts.model.User.Role.CUSTOMER);
        return ResponseEntity.ok(businessFunctionService.viewTransactionHistory(accountNumber));
    }

    @GetMapping("/{accountNumber}/statement")
    public ResponseEntity<String> generateStatement(@RequestHeader("X-User-Id") Long userId,
            @PathVariable String accountNumber) {
        checkPermission(userId, accountNumber, com.example.coreaccounts.model.User.Role.ADMIN,
                com.example.coreaccounts.model.User.Role.CUSTOMER);
        return ResponseEntity.ok(businessFunctionService.generateAccountStatement(accountNumber));
    }

    // --- Technical Function Services ---

    @GetMapping("/{accountNumber}/status")
    public ResponseEntity<String> verifyAccountStatus(@RequestHeader("X-User-Id") Long userId,
            @PathVariable String accountNumber) {
        checkPermission(userId, accountNumber, com.example.coreaccounts.model.User.Role.ADMIN);
        return ResponseEntity.ok(technicalFunctionService.verifyAccountStatus(accountNumber));
    }

    @PostMapping("/{accountNumber}/lock")
    public ResponseEntity<String> lockAccountFunds(@RequestHeader("X-User-Id") Long userId,
            @PathVariable String accountNumber,
            @RequestBody Map<String, BigDecimal> request) {
        checkPermission(userId, accountNumber, com.example.coreaccounts.model.User.Role.ADMIN);
        technicalFunctionService.lockAccountFunds(accountNumber, request.get("amount"));
        return ResponseEntity.ok("Funds locked");
    }

    @PostMapping("/{accountNumber}/credit")
    public ResponseEntity<String> creditAccount(@RequestHeader("X-User-Id") Long userId,
            @PathVariable String accountNumber,
            @RequestBody Map<String, BigDecimal> request) {
        // Only Admin, System (M2), or Clerk can credit.
        checkPermission(userId, accountNumber, com.example.coreaccounts.model.User.Role.ADMIN,
                com.example.coreaccounts.model.User.Role.OPERATIONS_CLERK);
        businessTransactionService.deposit(accountNumber, request.get("amount"));
        return ResponseEntity.ok("Account credited");
    }
}
