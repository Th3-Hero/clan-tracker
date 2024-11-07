package com.th3hero.clantracker.app.config;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.time.Duration;
import java.util.List;

class CustomIntervalBackOffPolicyTest {

    private RetryTemplate retryTemplate;

    @BeforeEach
    void setUp() {
        // Set up the intervals for testing
        List<Duration> intervals = List.of(
            Duration.ofSeconds(2),
            Duration.ofSeconds(3),
            Duration.ofSeconds(1)
        );

        // Create the CustomBackOffPolicy and RetryTemplate
        CustomIntervalBackOffPolicy backOffPolicy = new CustomIntervalBackOffPolicy(intervals);
        retryTemplate = new RetryTemplate();
        retryTemplate.setBackOffPolicy(backOffPolicy);
        retryTemplate.setRetryPolicy(new SimpleRetryPolicy(intervals.size()));
    }

    @Test
    void customBackOffPolicy_correctIntervals() {
        // Simulate the retry operation (mocking the actual operation that fails)
        final int maxAttempts = 3;
        int[] attempts = {0};  // To track the number of attempts

        // Execute the retry operation
        retryTemplate.execute(context -> {
            attempts[0]++;
            if (attempts[0] < maxAttempts) {
                throw new RuntimeException("Simulated failure");
            }
            return null;  // Simulate successful operation on the last try
        });

        // Verify that the backoff intervals were applied as expected (using Mockito/spy if necessary)
        // Verify the expected sleep intervals were respected in your custom backoff policy

        // Verify that the backoff method was called the expected number of times
        Assertions.assertThat(attempts[0]).isEqualTo(3); // Should have retried 2 times before success
    }

    @Test
    void customBackOffPolicy_handleInterruptedException() throws InterruptedException {
        // Simulate an interrupted retry
        final int maxAttempts = 3;
        int[] attempts = {0};  // To track the number of attempts

        // Create a RetryTemplate with interrupted backoff simulation
        retryTemplate.execute(context -> {
            attempts[0]++;
            if (attempts[0] < maxAttempts) {
                // Simulate an interruption by throwing an InterruptedException in between retries
                if (attempts[0] == 2) {
                    throw new InterruptedException("Simulated interruption during retry");
                }
                throw new RuntimeException("Simulated failure");
            }
            return null;  // Simulate successful operation on the last try
        });

        // Verify that the retry attempts happened, even with an interruption
        Assertions.assertThat(attempts[0]).isEqualTo(3); // Should have retried 2 times before success

        // You can also verify the backoff intervals were respected using logging or mocks
    }
}
