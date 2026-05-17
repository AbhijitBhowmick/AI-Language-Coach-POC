package com.platform.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final com.platform.common.security.JwtAuthenticationFilter jwtAuthenticationFilter;
    private final com.platform.common.security.SecurityProperties securityProperties;
    private final com.platform.common.security.RestAuthenticationEntryPoint authenticationEntryPoint;
    private final com.platform.common.security.RestAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(com.platform.common.security.JwtAuthenticationFilter jwtAuthenticationFilter,
                          com.platform.common.security.SecurityProperties securityProperties,
                          com.platform.common.security.RestAuthenticationEntryPoint authenticationEntryPoint,
                          com.platform.common.security.RestAccessDeniedHandler accessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.securityProperties = securityProperties;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler))
            .authorizeHttpRequests(auth -> {
                if (securityProperties.getPermitAllPaths() != null && !securityProperties.getPermitAllPaths().isEmpty()) {
                    auth.requestMatchers(securityProperties.getPermitAllPaths().toArray(new String[0])).permitAll();
                } else {
                    auth.requestMatchers("/auth/register", "/auth/login", "/auth/validate", "/actuator/**", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll();
                }
                auth.anyRequest().authenticated();
            })
            .addFilterBefore(jwtAuthenticationFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
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