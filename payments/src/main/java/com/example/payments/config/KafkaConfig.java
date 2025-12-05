package com.example.payments.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic transactionProcessingTopic() {
        return TopicBuilder.name("transaction_processing").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic paymentClearingTopic() {
        return TopicBuilder.name("payment_clearing").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic notificationServiceTopic() {
        return TopicBuilder.name("notification_service").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic securityAlertsTopic() {
        return TopicBuilder.name("security_alerts").partitions(1).replicas(1).build();
    }
}
