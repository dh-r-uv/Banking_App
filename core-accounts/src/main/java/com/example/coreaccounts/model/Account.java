package com.example.coreaccounts.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Data
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String accountNumber;

    private String ownerName;
    private Long userId;

    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    public enum AccountStatus {
        ACTIVE, LOCKED, SUSPENDED
    }
}
