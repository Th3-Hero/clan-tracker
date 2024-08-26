package com.th3hero.clantracker.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFiIterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorizedRequests ->
                authorizedRequests
                    .anyRequest().access(authorizedRequestManager())
            );
        return http.build();
    }

    private AuthorizationManager<RequestAuthorizationContext> authorizedRequestManager() {
        return (authentication, context) -> {
            String requestUrl = context.getRequest().getRequestURL().toString();
            String requestUri = context.getRequest().getRequestURI();
            String cfHeader = context.getRequest().getHeader("CF-Connecting-IP");
            String remoteAddr = context.getRequest().getRemoteAddr();

            boolean isLocalRequest = cfHeader == null &&
                (remoteAddr.startsWith("192.168.") || remoteAddr.startsWith("127.0."));
            boolean isCloudflareRequest = cfHeader != null &&
                requestUrl.startsWith("http://clan-tracker-api.the-hero.dev");

            if (requestUri.startsWith("/data/")) {
                return new AuthorizationDecision(isLocalRequest || isCloudflareRequest);
            } else {
                return new AuthorizationDecision(isLocalRequest);
            }
        };
    }
}
