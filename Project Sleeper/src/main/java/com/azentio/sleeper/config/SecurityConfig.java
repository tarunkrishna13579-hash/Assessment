package com.azentio.sleeper.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public UserDetailsService userDetailsService() {

        var ingestUser = User.withUsername("ingest")
            .password("{noop}ingest123")
            .roles("INGEST")
            .build();

        var investigatorUser = User.withUsername("investigator")
            .password("{noop}investigator123")
            .roles("INVESTIGATOR")
            .build();

        return new InMemoryUserDetailsManager(
            ingestUser,
            investigatorUser
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/core-events"
                ).hasRole("INGEST")

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/alerts/**",
                    "/api/accounts/**"
                ).hasRole("INVESTIGATOR")

                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}