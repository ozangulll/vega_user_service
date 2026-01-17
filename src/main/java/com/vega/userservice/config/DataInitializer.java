package com.vega.userservice.config;

import com.vega.userservice.model.User;
import com.vega.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Data Initializer
 * Creates default user on application startup if not exists
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(ApplicationArguments args) {
        // Create default user: versionengineai / versionengineai
        if (!userRepository.existsByUsername("versionengineai")) {
            User defaultUser = User.builder()
                    .username("versionengineai")
                    .email("versionengineai@vega.local")
                    .passwordHash(passwordEncoder.encode("versionengineai"))
                    .firstName("Version")
                    .lastName("Engine AI")
                    .role(User.Role.USER)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            userRepository.save(defaultUser);
            log.info("✅ Default user created: versionengineai / versionengineai");
        } else {
            log.info("ℹ️  Default user already exists: versionengineai");
        }
    }
}
