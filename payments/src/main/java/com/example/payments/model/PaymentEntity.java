package com.example.payments.model;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table("payments")
@Data
public class PaymentEntity {
    @PrimaryKey("transaction_id")
    private String transactionId;

    @Column("source_account")
    private String sourceAccount;

    @Column("target_account")
    private String targetAccount;

    private BigDecimal amount;
    private String status;
    private LocalDateTime timestamp;
    private String type;

    @Column("user_id")
    private Long userId;
}
