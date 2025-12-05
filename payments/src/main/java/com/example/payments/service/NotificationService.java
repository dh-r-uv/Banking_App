package com.example.payments.service;

import com.example.payments.model.PaymentEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @KafkaListener(topics = "notification_service", groupId = "notification-group")
    public void handlePaymentSuccessful(PaymentEvent event) {
        System.out.println("Customer UI: Notification - Payment Successful for " + event.getTransactionId());
    }
}
