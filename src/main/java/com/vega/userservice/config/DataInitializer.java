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
import java.util.List;

/**
 * Data Initializer
 * Creates default application users on startup if they do not exist.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private record SeedUser(String username, String password, String email, String firstName, String lastName) {}

    private static final List<SeedUser> SEED_USERS = List.of(
            new SeedUser("versionengineai", "versionengineai", "versionengineai@vega.local", "Version", "Engine AI"),
            new SeedUser("defaultuser", "defaultuser", "defaultuser@vega.local", "Default", "User"),
            new SeedUser("developer1", "developer1", "developer1@vega.local", "Developer", "One"),
            new SeedUser("reviewer1", "reviewer1", "reviewer1@vega.local", "Reviewer", "One")
    );

    @Override
    public void run(ApplicationArguments args) {
        LocalDateTime now = LocalDateTime.now();
        for (SeedUser seed : SEED_USERS) {
            if (!userRepository.existsByUsername(seed.username())) {
                User user = User.builder()
                        .username(seed.username())
                        .email(seed.email())
                        .passwordHash(passwordEncoder.encode(seed.password()))
                        .firstName(seed.firstName())
                        .lastName(seed.lastName())
                        .role(User.Role.USER)
                        .isActive(true)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();
                userRepository.save(user);
                log.info("✅ Seed user created: {} / {}", seed.username(), seed.password());
            } else {
                log.info("ℹ️  Seed user already exists: {}", seed.username());
            }
        }
    }
}
