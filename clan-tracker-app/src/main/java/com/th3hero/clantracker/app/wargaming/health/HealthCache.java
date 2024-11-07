package com.th3hero.clantracker.app.wargaming.health;

import org.springframework.boot.actuate.health.Health;

import java.time.Instant;

/**
 * Caches a {@link Health} result for later re-use.
 *
 * @param health
 *      The result to re-use
 * @param expiration
 *      When the result expires
 */
public record HealthCache(Health health, Instant expiration) {
}
