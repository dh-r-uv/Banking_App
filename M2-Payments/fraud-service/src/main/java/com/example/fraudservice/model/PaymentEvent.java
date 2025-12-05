package com.example.fraudservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentEvent {
    private String transactionId;
    private String sourceAccount;
    private String targetAccount;
    private BigDecimal amount;
    private String status;
    private String type;
    private Long userId;
}
