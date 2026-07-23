package com.cts.adstudio.iam.config;

import com.cts.adstudio.iam.entity.User;
import com.cts.adstudio.iam.enums.Role;
import com.cts.adstudio.iam.enums.UserStatus;
import com.cts.adstudio.iam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Seeds a default ADMIN user on first startup so the secured audit-log
 * endpoints can be exercised immediately. For local/dev use only.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner seedDefaultAdmin() {
        return args -> {
            final String adminEmail = "admin@adstudio.com";
            if (!userRepository.existsByEmail(adminEmail)) {
                User admin = User.builder()
                        .name("System Administrator")
                        .email(adminEmail)
                        .password(passwordEncoder.encode("password"))
                        .phone("0000000000")
                        .role(Role.ADMIN)
                        .status(UserStatus.ACTIVE)
                        .accountId(1L)
                        .build();
                userRepository.save(admin);
                log.warn("Seeded default ADMIN [{}] with password 'password' - CHANGE THIS IMMEDIATELY.", adminEmail);
            }
        };
    }
}
