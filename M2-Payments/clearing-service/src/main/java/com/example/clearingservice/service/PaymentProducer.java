package com.example.clearingservice.service;

import com.example.clearingservice.model.PaymentEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@lombok.extern.slf4j.Slf4j
public class PaymentProducer {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public PaymentProducer(KafkaTemplate<String, PaymentEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendNotification(PaymentEvent event) {
        // System.out.println("Publishing Event: Notification");
        log.info("[BANKING-CORE] Event Released: Notification for transactionId={}", event.getTransactionId());
        kafkaTemplate.send("notification_service", event);
    }
}
