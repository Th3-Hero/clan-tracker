package com.th3hero.clantracker.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                    .requestMatchers("/data/**").permitAll()
                    .anyRequest().access(localhostAuthorizationManager())
            );
        return http.build();
    }

    private AuthorizationManager<RequestAuthorizationContext> localhostAuthorizationManager() {
        return (authentication, context) -> {
            String remoteAddr = context.getRequest().getRemoteAddr();
            boolean isLocalhost = "127.0.0.1".equals(remoteAddr) || "::1".equals(remoteAddr);
            return new AuthorizationDecision(isLocalhost);
        };
    }
}
