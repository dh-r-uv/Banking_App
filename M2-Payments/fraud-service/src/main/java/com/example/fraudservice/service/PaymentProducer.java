package com.example.fraudservice.service;

import com.example.fraudservice.model.PaymentEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@lombok.extern.slf4j.Slf4j
public class PaymentProducer {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public PaymentProducer(KafkaTemplate<String, PaymentEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPaymentClearing(PaymentEvent event) {
        // System.out.println("Publishing Event: Payment Cleared");
        log.info("[BANKING-CORE] Event Released: Payment Cleared for transactionId={}", event.getTransactionId());
        kafkaTemplate.send("payment_clearing", event);
    }

    public void sendSecurityAlert(PaymentEvent event) {
        // System.out.println("Publishing Event: Security Alert");
        log.info("[BANKING-CORE] Event Released: Security Alert for transactionId={}", event.getTransactionId());
        kafkaTemplate.send("security_alerts", event);
    }
}
