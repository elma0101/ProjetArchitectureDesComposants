package com.bookstore.gateway.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

/**
 * Client for communicating with the User Management Service
 */
@Component
public class UserServiceClient {

    private final WebClient webClient;

    @Value("${services.user-management.url:http://localhost:8081}")
    private String userServiceUrl;

    public UserServiceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
            .baseUrl(userServiceUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    /**
     * Validate JWT token with the User Management Service
     * 
     * @param token JWT token to validate
     * @return Mono containing validation result with user details
     */
    public Mono<TokenValidationResponse> validateToken(String token) {
        return webClient.post()
            .uri("/api/auth/validate")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .retrieve()
            .bodyToMono(TokenValidationResponse.class)
            .timeout(Duration.ofSeconds(3))
            .onErrorResume(e -> Mono.empty());
    }

    /**
     * Get user details by username
     * 
     * @param username Username to lookup
     * @param token JWT token for authentication
     * @return Mono containing user details
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getUserByUsername(String username, String token) {
        return webClient.get()
            .uri("/api/users/username/{username}", username)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .retrieve()
            .bodyToMono(Map.class)
            .map(map -> (Map<String, Object>) map)
            .timeout(Duration.ofSeconds(3))
            .onErrorResume(e -> Mono.empty());
    }

    public static class TokenValidationResponse {
        private boolean valid;
        private String username;
        private String roles;
        private String userId;

        public TokenValidationResponse() {
        }

        public TokenValidationResponse(boolean valid, String username, String roles, String userId) {
            this.valid = valid;
            this.username = username;
            this.roles = roles;
            this.userId = userId;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getRoles() {
            return roles;
        }

        public void setRoles(String roles) {
            this.roles = roles;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
    }
}
