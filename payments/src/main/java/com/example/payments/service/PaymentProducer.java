package com.example.payments.service;

import com.example.payments.model.PaymentEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentProducer {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public PaymentProducer(KafkaTemplate<String, PaymentEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPaymentInitiated(PaymentEvent event) {
        System.out.println("Publishing Event: Payment Initiated");
        kafkaTemplate.send("transaction_processing", event);
    }

    public void sendFraudCheckPassed(PaymentEvent event) {
        System.out.println("Publishing Event: Fraud Check Passed");
        kafkaTemplate.send("payment_clearing", event);
    }

    public void sendPaymentSuccessful(PaymentEvent event) {
        System.out.println("Publishing Event: Payment Successful");
        kafkaTemplate.send("notification_service", event);
    }

    public void sendFraudAlert(PaymentEvent event) {
        System.out.println("Publishing Event: Fraud Alert");
        kafkaTemplate.send("security_alerts", event);
    }
}
