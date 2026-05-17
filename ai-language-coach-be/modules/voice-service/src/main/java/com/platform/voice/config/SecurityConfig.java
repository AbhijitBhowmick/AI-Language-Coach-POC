package com.platform.voice.config;

import com.platform.common.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtUtils jwtUtils;
    private final com.platform.common.security.SecurityProperties securityProperties;

    public SecurityConfig(JwtUtils jwtUtils,
                          com.platform.common.security.SecurityProperties securityProperties) {
        this.jwtUtils = jwtUtils;
        this.securityProperties = securityProperties;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> {
                if (securityProperties.getPermitAllPaths() != null && !securityProperties.getPermitAllPaths().isEmpty()) {
                    auth.requestMatchers(securityProperties.getPermitAllPaths().toArray(new String[0])).permitAll();
                } else {
                    auth.requestMatchers("/actuator/**", "/api/v1/voice/health", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll();
                }
                auth.anyRequest().authenticated();
            })
            .addFilterBefore(new JwtWebSocketFilter(jwtUtils), UsernamePasswordAuthenticationFilter.class);

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

    private static class JwtWebSocketFilter extends OncePerRequestFilter {
        private final JwtUtils jwtUtils;

        public JwtWebSocketFilter(JwtUtils jwtUtils) {
            this.jwtUtils = jwtUtils;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            filterChain.doFilter(request, response);
        }
    }
}