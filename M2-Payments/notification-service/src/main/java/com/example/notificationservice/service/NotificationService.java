package com.example.notificationservice.service;

import com.example.notificationservice.model.PaymentEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@lombok.extern.slf4j.Slf4j
public class NotificationService {

    @KafkaListener(topics = "notification_service", groupId = "notification-group")
    public void notifyUser(PaymentEvent event) {
        // System.out.println("NOTIFICATION: Payment Successful!");
        // System.out.println("Transaction ID: " + event.getTransactionId());
        // System.out.println("Amount: " + event.getAmount());
        // System.out.println("User ID: " + event.getUserId());
        log.info("[BANKING-CORE] Event Received: Notification for transactionId={}", event.getTransactionId());
        log.info("[BANKING-CORE] Processed: Notifying User userId={} about successful payment of amount={}",
                event.getUserId(),
                event.getAmount());
    }
}
