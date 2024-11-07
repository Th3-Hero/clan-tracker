package com.th3hero.clantracker.app.wargaming.health;

import com.th3hero.clantracker.app.wargaming.ClanSearch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.MediaType;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * A {@link HealthIndicator} that probes the Wargaming API. Reports {@link org.springframework.boot.actuate.health.Status#UP UP}
 * when receiving {@code "ok"} responses, otherwise reports {@link org.springframework.boot.actuate.health.Status#DOWN DOWN}.
 */
@Slf4j
class WargamingApiHealthIndicator extends AbstractCachableHealthIndicator {

    private final String apiToken;
    private final RestClient restClient;

    public WargamingApiHealthIndicator(String apiToken, RestClient restClient) {
        super(Duration.of(2, ChronoUnit.MINUTES));
        this.apiToken = apiToken;
        this.restClient = restClient;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        String status;
        try {
            String uri = "https://api.worldoftanks.com/wot/clans/list/?application_id=%s&search=%s&fields=%s".formatted(
                apiToken,
                "HRTBT",
                "clan_id"
            );

            var body = restClient.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(ClanSearch.class);

            if (body != null && "ok".equalsIgnoreCase(body.status())) {
                builder.up();
                status = body.status();
            } else {
                // Even though the API may be up, a non-ok response potentially means requests are not working
                builder.down();
                status = "error";
            }
        } catch (RestClientResponseException ex) {
            builder.down(ex);
            status = "error";
        } catch (ResourceAccessException ex) {
            builder.down(ex);
            status = "unknown host";
        }
        builder.withDetail("status", status);
    }
}
