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
 * Creates {@linkplain SeedUsers dev seed users} on startup if they do not exist.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        LocalDateTime now = LocalDateTime.now();
        for (SeedUsers.Spec seed : SeedUsers.all()) {
            if (!userRepository.existsByUsername(seed.username())) {
                User user = User.builder()
                        .username(seed.username())
                        .email(seed.email())
                        .passwordHash(passwordEncoder.encode(seed.plainPassword()))
                        .firstName(seed.firstName())
                        .lastName(seed.lastName())
                        .role(User.Role.USER)
                        .isActive(true)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();
                userRepository.save(user);
                log.info("✅ Seed user created: {} / {}", seed.username(), seed.plainPassword());
            } else {
                log.info("ℹ️  Seed user already exists: {}", seed.username());
            }
        }
    }
}
