package com.bookstore.gateway.filter;

import com.bookstore.gateway.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private GatewayFilterChain chain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter();
        filter.jwtUtil = jwtUtil;
    }

    @Test
    void shouldAllowRequestsToExcludedPaths() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
            .get("/api/auth/login")
            .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = filter.apply(new JwtAuthenticationFilter.Config())
            .filter(exchange, chain);

        // Then
        StepVerifier.create(result)
            .verifyComplete();
        
        verify(chain).filter(exchange);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void shouldRejectRequestsWithoutAuthorizationHeader() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
            .get("/api/users/1")
            .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        Mono<Void> result = filter.apply(new JwtAuthenticationFilter.Config())
            .filter(exchange, chain);

        // Then
        StepVerifier.create(result)
            .verifyComplete();
        
        verifyNoInteractions(chain);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void shouldRejectRequestsWithInvalidToken() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
            .get("/api/users/1")
            .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
            .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(jwtUtil.validateToken("invalid-token")).thenReturn(false);

        // When
        Mono<Void> result = filter.apply(new JwtAuthenticationFilter.Config())
            .filter(exchange, chain);

        // Then
        StepVerifier.create(result)
            .verifyComplete();
        
        verifyNoInteractions(chain);
        verify(jwtUtil).validateToken("invalid-token");
    }

    @Test
    void shouldAllowRequestsWithValidToken() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
            .get("/api/users/1")
            .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
            .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(jwtUtil.validateToken("valid-token")).thenReturn(true);
        when(jwtUtil.getUsernameFromToken("valid-token")).thenReturn("testuser");
        when(jwtUtil.getRolesFromToken("valid-token")).thenReturn("USER,ADMIN");
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // When
        Mono<Void> result = filter.apply(new JwtAuthenticationFilter.Config())
            .filter(exchange, chain);

        // Then
        StepVerifier.create(result)
            .verifyComplete();
        
        verify(chain).filter(any(ServerWebExchange.class));
        verify(jwtUtil).validateToken("valid-token");
        verify(jwtUtil).getUsernameFromToken("valid-token");
        verify(jwtUtil).getRolesFromToken("valid-token");
    }
}