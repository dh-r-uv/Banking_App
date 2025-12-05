package com.example.payments.controller;

import com.example.payments.model.PaymentEvent;
import com.example.payments.service.PaymentProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentProducer paymentProducer;

    public PaymentController(PaymentProducer paymentProducer) {
        this.paymentProducer = paymentProducer;
    }

    @PostMapping
    public ResponseEntity<String> initiatePayment(@RequestHeader("X-User-Id") Long userId,
            @RequestBody PaymentEvent event) {
        event.setTransactionId(UUID.randomUUID().toString());
        event.setStatus("INITIATED");
        event.setType("TRANSFER");
        event.setUserId(userId);
        paymentProducer.sendPaymentInitiated(event);
        return ResponseEntity.ok("Payment Initiated: " + event.getTransactionId());
    }

    @PostMapping("/deposit")
    public ResponseEntity<String> initiateDeposit(@RequestHeader("X-User-Id") Long userId,
            @RequestBody PaymentEvent event) {
        event.setTransactionId(UUID.randomUUID().toString());
        event.setStatus("INITIATED");
        event.setType("DEPOSIT");
        event.setUserId(userId);
        // For deposit, sourceAccount might be "PARTNER" or "CASH"
        if (event.getSourceAccount() == null) {
            event.setSourceAccount("PARTNER_DEPOSIT");
        }
        paymentProducer.sendPaymentInitiated(event);
        return ResponseEntity.ok("Deposit Initiated: " + event.getTransactionId());
    }
}
