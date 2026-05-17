package com.platform.community.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final com.platform.common.security.SecurityProperties securityProperties;

    public SecurityConfig(com.platform.common.security.SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> {
                if (securityProperties.getPermitAllPaths() != null && !securityProperties.getPermitAllPaths().isEmpty()) {
                    auth.requestMatchers(securityProperties.getPermitAllPaths().toArray(new String[0])).permitAll();
                } else {
                    auth.requestMatchers("/community/**").permitAll();
                }
                auth.anyRequest().permitAll();
            });
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(securityProperties.getCors().getAllowedOrigins());
        configuration.setAllowedMethods(securityProperties.getCors().getAllowedMethods());
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(securityProperties.getCors().getMapping(), configuration);
        return source;
    }
}