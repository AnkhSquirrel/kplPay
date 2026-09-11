package com.ankhsquirrel.kplpay.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Exposes the system clock as an injectable bean, so services never call {@code LocalDate.now()}
 * / {@code Instant.now()} directly (a static, hidden dependency that's also hard to control in
 * tests). {@link Clock#systemDefaultZone()} matches the behavior of a bare {@code now()} call.
 */
@Configuration
class ClockConfig {

    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }
}
