
package com.example.clearingservice.service;

import com.example.clearingservice.model.PaymentEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

@Service
public class ClearingService {

    private final PaymentProducer paymentProducer;
    private final com.example.clearingservice.repository.PaymentRepository paymentRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    // M1 Business Transaction Service URL
    private final String M1_URL = "http://localhost:8084/api/business/transactions";

    public ClearingService(PaymentProducer paymentProducer,
            com.example.clearingservice.repository.PaymentRepository paymentRepository) {
        this.paymentProducer = paymentProducer;
        this.paymentRepository = paymentRepository;
    }

    @KafkaListener(topics = "payment_clearing", groupId = "clearing-group")
    public void settlePayment(PaymentEvent event) {
        System.out.println("Clearing Service: Settling " + event.getTransactionId());

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String url;
            Map<String, Object> request = new HashMap<>();

            if ("DEPOSIT".equalsIgnoreCase(event.getType())) {
                url = M1_URL + "/deposit";
                request.put("accountNumber", event.getTargetAccount());
                request.put("amount", event.getAmount());
            } else {
                url = M1_URL + "/transfer";
                request.put("fromAccount", event.getSourceAccount());
                request.put("toAccount", event.getTargetAccount());
                request.put("amount", event.getAmount());
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            restTemplate.postForEntity(url, entity, String.class);

            event.setStatus("SUCCESS");
            updatePaymentStatus(event.getTransactionId(), "SUCCESS");
            paymentProducer.sendNotification(event);

        } catch (Exception e) {
            System.err.println("Settlement Failed: " + e.getMessage());
            event.setStatus("FAILED");
            updatePaymentStatus(event.getTransactionId(), "FAILED");
            // Optionally send notification about failure
        }
    }

    private void updatePaymentStatus(String transactionId, String status) {
        paymentRepository.findById(transactionId).ifPresent(payment -> {
            payment.setStatus(status);
            paymentRepository.save(payment);
        });
    }
}
