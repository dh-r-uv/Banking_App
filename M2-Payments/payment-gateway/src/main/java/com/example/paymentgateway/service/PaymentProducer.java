package com.example.paymentgateway.service;

import com.example.paymentgateway.model.PaymentEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@lombok.extern.slf4j.Slf4j
public class PaymentProducer {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public PaymentProducer(KafkaTemplate<String, PaymentEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPaymentInitiated(PaymentEvent event) {
        // System.out.println("Publishing Event: Payment Initiated");
        log.info("[BANKING-CORE] Event Released: Payment Initiated for transactionId={}", event.getTransactionId());
        kafkaTemplate.send("transaction_processing", event);
    }
}
