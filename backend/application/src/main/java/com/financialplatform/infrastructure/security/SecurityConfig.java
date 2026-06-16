package com.financialplatform.infrastructure.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    private final JwtProperties jwtProperties;
    private final SecurityProblemDetailsHandler securityProblemDetailsHandler;

    public SecurityConfig(
            JwtProperties jwtProperties,
            SecurityProblemDetailsHandler securityProblemDetailsHandler) {
        this.jwtProperties = jwtProperties;
        this.securityProblemDetailsHandler = securityProblemDetailsHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);

        if (jwtProperties.isEnabled()) {
            http.authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/v1/auth/login").permitAll()
                            .anyRequest().authenticated())
                    .exceptionHandling(ex -> ex
                            .authenticationEntryPoint(securityProblemDetailsHandler)
                            .accessDeniedHandler(securityProblemDetailsHandler));
        } else {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        }

        return http.build();
    }
}
