package com.example.payments.service;

import com.example.payments.model.PaymentEvent;
import com.example.payments.repository.PaymentRepository;
import com.example.payments.model.PaymentEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ClearingService {

    private final PaymentProducer paymentProducer;
    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public ClearingService(PaymentProducer paymentProducer, PaymentRepository paymentRepository) {
        this.paymentProducer = paymentProducer;
        this.paymentRepository = paymentRepository;
    }

    @KafkaListener(topics = "payment_clearing", groupId = "clearing-group")
    public void handleFraudCheckPassed(PaymentEvent event) {
        System.out.println("Backend: Clearing and Settlement for " + event.getTransactionId());
        savePayment(event, "CLEARING");

        try {
            // Prepare headers with User ID from event
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("X-User-Id", String.valueOf(event.getUserId()));
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            if ("DEPOSIT".equalsIgnoreCase(event.getType())) {
                // Call M1 to credit account
                String m1Url = "http://localhost:8081/api/accounts/" + event.getTargetAccount() + "/credit";
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("amount", event.getAmount());

                org.springframework.http.HttpEntity<Map<String, Object>> entity = new org.springframework.http.HttpEntity<>(
                        requestBody, headers);
                restTemplate.postForEntity(m1Url, entity, String.class);
            } else {
                // Default to TRANSFER
                String m1Url = "http://localhost:8081/api/accounts/transfer";
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("fromAccount", event.getSourceAccount());
                requestBody.put("toAccount", event.getTargetAccount());
                requestBody.put("amount", event.getAmount());

                org.springframework.http.HttpEntity<Map<String, Object>> entity = new org.springframework.http.HttpEntity<>(
                        requestBody, headers);
                restTemplate.postForEntity(m1Url, entity, String.class);
            }

            System.out.println("Payment/Deposit Successful");
            event.setStatus("SUCCESS");
            paymentProducer.sendPaymentSuccessful(event);
            savePayment(event, "SUCCESS");
        } catch (org.springframework.web.client.RestClientResponseException e) {
            System.err.println("Clearing Failed: " + e.getMessage());
            System.err.println("Response Body: " + e.getResponseBodyAsString());
            event.setStatus("FAILED");
            savePayment(event, "FAILED");
        } catch (Exception e) {
            System.err.println("Clearing Failed: " + e.getMessage());
            e.printStackTrace();
            event.setStatus("FAILED");
            savePayment(event, "FAILED");
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
