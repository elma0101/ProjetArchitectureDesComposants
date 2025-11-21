package com.bookstore.config;

import com.bookstore.security.JwtAuthenticationFilter;
import com.bookstore.security.RateLimitingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Autowired
    private RateLimitingFilter rateLimitingFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/error").permitAll()
                
                // Read-only endpoints for authenticated users
                .requestMatchers("GET", "/api/books/**").hasAnyRole("USER", "LIBRARIAN", "ADMIN")
                .requestMatchers("GET", "/api/authors/**").hasAnyRole("USER", "LIBRARIAN", "ADMIN")
                .requestMatchers("GET", "/api/recommendations/**").hasAnyRole("USER", "LIBRARIAN", "ADMIN")
                
                // Loan operations for users and above
                .requestMatchers("POST", "/api/loans").hasAnyRole("USER", "LIBRARIAN", "ADMIN")
                .requestMatchers("PUT", "/api/loans/*/return").hasAnyRole("USER", "LIBRARIAN", "ADMIN")
                .requestMatchers("GET", "/api/loans/**").hasAnyRole("USER", "LIBRARIAN", "ADMIN")
                
                // Book and author management for librarians and admins
                .requestMatchers("POST", "/api/books").hasAnyRole("LIBRARIAN", "ADMIN")
                .requestMatchers("PUT", "/api/books/**").hasAnyRole("LIBRARIAN", "ADMIN")
                .requestMatchers("PATCH", "/api/books/**").hasAnyRole("LIBRARIAN", "ADMIN")
                .requestMatchers("DELETE", "/api/books/**").hasRole("ADMIN")
                
                .requestMatchers("POST", "/api/authors").hasAnyRole("LIBRARIAN", "ADMIN")
                .requestMatchers("PUT", "/api/authors/**").hasAnyRole("LIBRARIAN", "ADMIN")
                .requestMatchers("PATCH", "/api/authors/**").hasAnyRole("LIBRARIAN", "ADMIN")
                .requestMatchers("DELETE", "/api/authors/**").hasRole("ADMIN")
                
                // Administrative endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/audit/**").hasRole("ADMIN")
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                
                // Require authentication for all other requests
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.deny())
                .contentTypeOptions(contentTypeOptions -> {})
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true)
                )
                .referrerPolicy(referrerPolicy -> 
                    referrerPolicy.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
            );

        return http.build();
    }
}