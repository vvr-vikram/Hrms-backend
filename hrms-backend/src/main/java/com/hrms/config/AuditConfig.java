package com.hrms.config;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@EnableJpaAuditing(
    auditorAwareRef = "auditorAware",
    dateTimeProviderRef = "dateTimeProvider",
    modifyOnCreate = false
)
public class AuditConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            try {
                String username = SecurityContextHolder.getContext().getAuthentication().getName();
                return Optional.of(username != null && !username.equals("anonymousUser") ? username : "SYSTEM");
            } catch (Exception e) {
                return Optional.of("SYSTEM");
            }
        };
    }

    @Bean
    public DateTimeProvider dateTimeProvider() {
        return () -> Optional.of(OffsetDateTime.now());
    }
}