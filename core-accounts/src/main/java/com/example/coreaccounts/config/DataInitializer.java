package com.example.coreaccounts.config;

import com.example.coreaccounts.model.User;
import com.example.coreaccounts.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(UserRepository userRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                userRepository.save(new User(null, "admin", "admin123", User.Role.ADMIN));
                userRepository.save(new User(null, "alice", "alice123", User.Role.CUSTOMER));
                userRepository.save(new User(null, "bob", "bob123", User.Role.CUSTOMER));
                userRepository.save(new User(null, "clerk", "clerk123", User.Role.OPERATIONS_CLERK));
                System.out.println("Default users created: admin, alice, bob, clerk");
            }
        };
    }
}
