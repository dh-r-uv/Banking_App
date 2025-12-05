package com.example.payments.service;

import com.example.payments.model.PaymentEvent;
import com.example.payments.repository.PaymentRepository;
import com.example.payments.model.PaymentEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FraudService {

    private final PaymentProducer paymentProducer;
    private final PaymentRepository paymentRepository;

    public FraudService(PaymentProducer paymentProducer, PaymentRepository paymentRepository) {
        this.paymentProducer = paymentProducer;
        this.paymentRepository = paymentRepository;
    }

    @KafkaListener(topics = "transaction_processing", groupId = "fraud-group")
    public void handlePaymentInitiated(PaymentEvent event) {
        System.out.println("Backend: Fraud Check for " + event.getTransactionId());
        savePayment(event, "PROCESSING_FRAUD_CHECK");

        // Mock Fraud Logic
        if (event.getAmount().intValue() < 10000) {
            System.out.println("Fraud Check Passed");
            event.setStatus("FRAUD_CHECK_PASSED");
            paymentProducer.sendFraudCheckPassed(event);
            savePayment(event, "FRAUD_CHECK_PASSED");
        } else {
            System.out.println("Fraud Alert Raised");
            event.setStatus("FRAUD_DETECTED");
            paymentProducer.sendFraudAlert(event);
            savePayment(event, "FRAUD_DETECTED");
        }
    }

    private void savePayment(PaymentEvent event, String status) {
        PaymentEntity entity = new PaymentEntity();
        entity.setTransactionId(event.getTransactionId());
        entity.setSourceAccount(event.getSourceAccount());
        entity.setTargetAccount(event.getTargetAccount());
        entity.setAmount(event.getAmount());
        entity.setStatus(status);
        entity.setType(event.getType());
        entity.setUserId(event.getUserId());
        entity.setTimestamp(LocalDateTime.now());
        paymentRepository.save(entity);
    }
}
