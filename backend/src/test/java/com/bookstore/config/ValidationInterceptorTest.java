package com.bookstore.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationInterceptorTest {

    private ValidationInterceptor interceptor;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private Object handler;

    @BeforeEach
    void setUp() {
        interceptor = new ValidationInterceptor();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        handler = new Object();
    }

    @Test
    void shouldAllowHealthCheckEndpoints() throws Exception {
        // Given
        request.setRequestURI("/health");

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldAllowActuatorEndpoints() throws Exception {
        // Given
        request.setRequestURI("/actuator/health");

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldAllowValidRequest() throws Exception {
        // Given
        request.setRequestURI("/api/books");
        request.setParameter("title", "Clean Code");
        request.setParameter("author", "Robert Martin");

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldRejectRequestWithSqlInjection() throws Exception {
        // Given
        request.setRequestURI("/api/books");
        request.setParameter("title", "'; DROP TABLE books; --");

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertThat(result).isFalse();
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    void shouldRejectRequestWithXssAttempt() throws Exception {
        // Given
        request.setRequestURI("/api/books");
        request.setParameter("title", "<script>alert('xss')</script>");

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertThat(result).isFalse();
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    void shouldAllowRequestWithSafeHeaders() throws Exception {
        // Given
        request.setRequestURI("/api/books");
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Authorization", "Bearer token123");
        request.addHeader("User-Agent", "Mozilla/5.0");

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldSkipValidationForCommonHeaders() throws Exception {
        // Given
        request.setRequestURI("/api/books");
        request.addHeader("User-Agent", "Mozilla/5.0 (compatible; <script>)");
        request.addHeader("Accept", "text/html,application/xhtml+xml");

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldAllowEmptyParameters() throws Exception {
        // Given
        request.setRequestURI("/api/books");

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldHandleMultipleParameterValues() throws Exception {
        // Given
        request.setRequestURI("/api/books");
        request.setParameter("genres", new String[]{"Fiction", "Science"});

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldRejectMultipleParameterValuesWithMaliciousContent() throws Exception {
        // Given
        request.setRequestURI("/api/books");
        request.setParameter("genres", new String[]{"Fiction", "SELECT * FROM users"});

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertThat(result).isFalse();
        assertThat(response.getStatus()).isEqualTo(400);
    }
}