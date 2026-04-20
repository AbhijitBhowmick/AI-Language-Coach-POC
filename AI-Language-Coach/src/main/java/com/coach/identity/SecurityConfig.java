package com.coach.identity;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final JwtAuthenticationEntryPoint jwtAuthEntryPoint;
    private final TenantAuthenticationFilter tenantAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, 
                          JwtAuthenticationEntryPoint jwtAuthEntryPoint,
                          TenantAuthenticationFilter tenantAuthenticationFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.jwtAuthEntryPoint = jwtAuthEntryPoint;
        this.tenantAuthenticationFilter = tenantAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> {})
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> 
                ex.authenticationEntryPoint(jwtAuthEntryPoint))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/actuator/**",
                    "/auth/**",
                    "/login/**",
                    "/error",
                    "/",
                    "/css/**",
                    "/js/**",
                    "/login",
                    "/register"
                ).permitAll()
                .requestMatchers("/api/v1/coach/**").hasRole("STUDENT")
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/api/v1/auth/login")
                .defaultSuccessUrl("/api/v1/coach/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/api/v1/auth/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            );
            // Phase 2: Uncomment below to enable Keycloak
            // .oauth2Login(oauth2 -> oauth2
            //     .defaultSuccessUrl("/api/v1/auth/oauth2/success", true)
            // )
            // .oauth2ResourceServer(oauth2 -> oauth2
            //     .jwt(jwt -> {})
            // )
        
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}