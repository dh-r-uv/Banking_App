package com.example.fraudservice.repository;

import com.example.fraudservice.model.PaymentEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface PaymentRepository extends CassandraRepository<PaymentEntity, String> {
}
