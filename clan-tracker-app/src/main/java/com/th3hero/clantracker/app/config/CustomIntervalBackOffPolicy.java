package com.th3hero.clantracker.app.config;

import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.BackOffContext;
import org.springframework.retry.backoff.BackOffInterruptedException;
import org.springframework.retry.backoff.BackOffPolicy;

import java.time.Duration;
import java.util.List;

public class CustomIntervalBackOffPolicy implements BackOffPolicy {
    private final List<Long> intervals;
    private int attempt = 0;

    public CustomIntervalBackOffPolicy(List<Duration> intervals) {
        this.intervals = intervals.stream()
            .map(Duration::toMillis)
            .toList();
    }

    @Override
    public BackOffContext start(RetryContext var1) {
        this.attempt = 0; // Reset the attempt counter for each retry cycle
        return new BackOffContext() {
        };
    }

    @Override
    public void backOff(BackOffContext backOffContext) throws BackOffInterruptedException {
        if (attempt < intervals.size()) {
            try {
                // Sleep for the interval duration
                Thread.sleep(intervals.get(attempt));
            } catch (InterruptedException e) {
                // Re-interrupt the current thread
                Thread.currentThread().interrupt();
                // Rethrow the BackOffInterruptedException so that Spring Retry handles the interruption properly
                throw new BackOffInterruptedException("Backoff was interrupted", e);
            }
        }
        attempt++;
    }
}
