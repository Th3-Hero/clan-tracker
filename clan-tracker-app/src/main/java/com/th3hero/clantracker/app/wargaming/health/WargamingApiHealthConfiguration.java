package com.th3hero.clantracker.app.wargaming.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@ConditionalOnEnabledHealthIndicator("wargamingapi")
class WargamingApiHealthConfiguration {
    @Value("${wargaming.api-token}")
    private String apiToken;

    @Bean
    HealthContributor wargamingApiHealthIndicator(
        RestClient restClient
    ) {
        return new WargamingApiHealthIndicator(apiToken, restClient);
    }
}
