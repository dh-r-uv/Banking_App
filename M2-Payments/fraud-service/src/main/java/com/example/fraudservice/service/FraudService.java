package com.example.fraudservice.service;

import com.example.fraudservice.model.PaymentEntity;
import com.example.fraudservice.model.PaymentEvent;
import com.example.fraudservice.repository.PaymentRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@lombok.extern.slf4j.Slf4j
public class FraudService {

    private final PaymentRepository paymentRepository;
    private final PaymentProducer paymentProducer;

    public FraudService(PaymentRepository paymentRepository, PaymentProducer paymentProducer) {
        this.paymentRepository = paymentRepository;
        this.paymentProducer = paymentProducer;
    }

    @KafkaListener(topics = "transaction_processing", groupId = "fraud-group")
    public void processPayment(PaymentEvent event) {
        try {
            // System.out.println("Fraud Service: Processing " + event.getTransactionId());
            log.info("[BANKING-CORE] Event Received: Transaction Processing for transactionId={}",
                    event.getTransactionId());

            // Simple Fraud Logic: Amount > 10000 is suspicious
            if (event.getAmount().compareTo(new BigDecimal("10000")) > 0) {
                // System.out.println("Fraud Detected!");
                log.info("[BANKING-CORE] Processed: Fraud Detected for transactionId={}", event.getTransactionId());
                event.setStatus("FAILED");
                savePayment(event, "FRAUD_DETECTED");
                log.info("[BANKING-CORE] Action Triggered: Sending Security Alert for transactionId={}",
                        event.getTransactionId());
                paymentProducer.sendSecurityAlert(event);
            } else {
                // System.out.println("Fraud Check Passed");
                log.info("[BANKING-CORE] Processed: Fraud Check Passed for transactionId={}", event.getTransactionId());
                event.setStatus("CLEARED");
                savePayment(event, "FRAUD_CHECK_PASSED");
                log.info("[BANKING-CORE] Action Triggered: Sending Payment Clearing for transactionId={}",
                        event.getTransactionId());
                paymentProducer.sendPaymentClearing(event);
            }
        } catch (Exception e) {
            System.err.println("Error processing payment: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void savePayment(PaymentEvent event, String status) {
        PaymentEntity entity = new PaymentEntity();
        entity.setTransactionId(event.getTransactionId());
        entity.setSourceAccount(event.getSourceAccount());
        entity.setTargetAccount(event.getTargetAccount());
        entity.setAmount(event.getAmount());
        entity.setStatus(status);
        entity.setTimestamp(LocalDateTime.now());
        entity.setType(event.getType());
        entity.setUserId(event.getUserId());
        paymentRepository.save(entity);
    }
}
