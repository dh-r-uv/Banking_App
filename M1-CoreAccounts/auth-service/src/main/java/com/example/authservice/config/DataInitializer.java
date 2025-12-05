package com.example.authservice.config;

import com.example.authservice.model.User;
import com.example.authservice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@lombok.extern.slf4j.Slf4j
public class DataInitializer {

    @Bean
    CommandLineRunner initData(UserRepository userRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                userRepository.save(new User(null, "admin", "admin123", User.Role.ADMIN));
                userRepository.save(new User(null, "alice", "alice123", User.Role.CUSTOMER));
                userRepository.save(new User(null, "bob", "bob123", User.Role.CUSTOMER));
                userRepository.save(new User(null, "clerk", "clerk123", User.Role.OPERATIONS_CLERK));
                // System.out.println("Default users created: admin, alice, bob, clerk");
                log.info("[BANKING-CORE] Action Triggered: Default users created: admin, alice, bob, clerk");
            }
        };
    }
}
