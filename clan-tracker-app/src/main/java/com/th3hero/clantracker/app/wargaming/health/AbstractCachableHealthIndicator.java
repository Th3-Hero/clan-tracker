package com.th3hero.clantracker.app.wargaming.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;

import java.time.Duration;
import java.time.Instant;

/**
 * Base {@link HealthIndicator} implementation that provides simple result caching, reducing the frequency of new health checks.
 *
 * @see org.springframework.boot.actuate.health.AbstractHealthIndicator
 */
@Slf4j
public abstract class AbstractCachableHealthIndicator implements HealthIndicator {
    private HealthCache healthCache = new HealthCache(null, Instant.MIN);

    private final Duration upCacheDuration;
    private final Duration downCacheDuration;

    /**
     * Construct a new instance using the given {@link Duration} as the cache duration for any health status.
     *
     * @param cacheDuration
     *      The duration to cache health checks for
     */
    protected AbstractCachableHealthIndicator(Duration cacheDuration) {
        this.upCacheDuration = cacheDuration;
        this.downCacheDuration = cacheDuration;
    }

    /**
     * Construct a new instance using different {@link Duration}s for {@link Status#UP} and {@link Status#DOWN}/{@link Status#UNKNOWN}.
     *
     * @param upCacheDuration
     *      The duration to cache successful health checks for
     * @param downCacheDuration
     *      The duration to cache unsuccessful health checks for
     */
    protected AbstractCachableHealthIndicator(Duration upCacheDuration, Duration downCacheDuration) {
        this.upCacheDuration = upCacheDuration;
        this.downCacheDuration = downCacheDuration;
    }

    @Override
    public final Health health() {
        var now = Instant.now();
        // Re-use cached health if not yet expired
        if (now.isBefore(healthCache.expiration())) {
            log.debug("Using cached health check as the previous one is still valid. Currently {}, expiring {}.", now, healthCache.expiration());
            return healthCache.health();
        }

        Health.Builder builder = new Health.Builder();
        try {
            doHealthCheck(builder);
        }
        catch (Exception ex) {
            builder.down(ex);
            log.warn("Health check failed", ex);
        }

        var health = builder.build();
        // Any status other than UP is considered DOWN
        if (health.getStatus() == Status.UP) {
            healthCache = new HealthCache(health, now.plus(upCacheDuration));
        } else {
            healthCache = new HealthCache(health, now.plus(downCacheDuration));
        }
        return health;
    }

    /**
     * Actual health check logic.
     *
     * @param builder
     *      The {@link Health.Builder} to report health status and details
     * @throws Exception any {@link Exception} that should create a {@link Status#DOWN} system status.
     */
    protected abstract void doHealthCheck(Health.Builder builder) throws Exception;
}
