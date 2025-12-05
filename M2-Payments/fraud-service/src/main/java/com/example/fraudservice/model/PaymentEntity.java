package com.example.fraudservice.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
public class PaymentEntity {
    @Id
    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "source_account")
    private String sourceAccount;

    @Column(name = "target_account")
    private String targetAccount;

    private BigDecimal amount;
    private String status;
    private LocalDateTime timestamp;
    private String type;

    @Column(name = "user_id")
    private Long userId;
}
