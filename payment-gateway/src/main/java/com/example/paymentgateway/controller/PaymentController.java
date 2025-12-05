package com.example.paymentgateway.controller;

import com.example.paymentgateway.model.PaymentEvent;
import com.example.paymentgateway.service.PaymentProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentProducer paymentProducer;
    private final com.example.paymentgateway.repository.PaymentRepository paymentRepository;

    public PaymentController(PaymentProducer paymentProducer,
            com.example.paymentgateway.repository.PaymentRepository paymentRepository) {
        this.paymentProducer = paymentProducer;
        this.paymentRepository = paymentRepository;
    }

    @PostMapping
    public ResponseEntity<String> initiatePayment(@RequestHeader("X-User-Id") Long userId,
            @RequestBody Map<String, Object> request) {
        PaymentEvent event = new PaymentEvent();
        event.setTransactionId(UUID.randomUUID().toString());
        event.setSourceAccount((String) request.get("sourceAccount"));
        event.setTargetAccount((String) request.get("targetAccount"));
        event.setAmount(new BigDecimal(request.get("amount").toString()));
        event.setStatus("INITIATED");
        event.setType("TRANSFER");
        event.setUserId(userId);

        paymentProducer.sendPaymentInitiated(event);
        return ResponseEntity.ok(event.getTransactionId());
    }

    @PostMapping("/deposit")
    public ResponseEntity<String> initiateDeposit(@RequestHeader("X-User-Id") Long userId,
            @RequestBody Map<String, Object> request) {
        PaymentEvent event = new PaymentEvent();
        event.setTransactionId(UUID.randomUUID().toString());
        event.setSourceAccount("PARTNER_DEPOSIT");
        event.setTargetAccount((String) request.get("targetAccount"));
        event.setAmount(new BigDecimal(request.get("amount").toString()));
        event.setStatus("INITIATED");
        event.setType("DEPOSIT");
        event.setUserId(userId);

        paymentProducer.sendPaymentInitiated(event);
        return ResponseEntity.ok(event.getTransactionId());
    }

    @GetMapping("/{transactionId}/status")
    public ResponseEntity<Map<String, String>> getPaymentStatus(@PathVariable String transactionId) {
        return paymentRepository.findById(transactionId)
                .map(payment -> ResponseEntity.ok(Map.of("status", payment.getStatus())))
                .orElse(ResponseEntity.notFound().build());
    }
}
