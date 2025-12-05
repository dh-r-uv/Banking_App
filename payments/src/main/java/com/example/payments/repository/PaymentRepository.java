package com.example.payments.repository;

import com.example.payments.model.PaymentEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface PaymentRepository extends CassandraRepository<PaymentEntity, String> {
}
