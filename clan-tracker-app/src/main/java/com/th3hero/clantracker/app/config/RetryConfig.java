package com.th3hero.clantracker.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Configuration
@EnableRetry
public class RetryConfig {

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        List<Duration> intervals = List.of(
            Duration.of(2, ChronoUnit.SECONDS),
            Duration.of(30, ChronoUnit.SECONDS),
            Duration.of(5, ChronoUnit.MINUTES),
            Duration.of(10, ChronoUnit.MINUTES)
        );
        retryTemplate.setBackOffPolicy(new CustomIntervalBackOffPolicy(intervals));
        retryTemplate.setRetryPolicy(new SimpleRetryPolicy(intervals.size()));

        return retryTemplate;
    }
}
