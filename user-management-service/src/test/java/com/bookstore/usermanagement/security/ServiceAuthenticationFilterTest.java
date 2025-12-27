package com.bookstore.usermanagement.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceAuthenticationFilterTest {
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    @Mock
    private FilterChain filterChain;
    
    private ServiceAuthenticationFilter filter;
    
    private static final String VALID_API_KEY = "test-api-key";
    private static final String ALLOWED_SERVICES = "api-gateway,book-catalog-service";
    
    @BeforeEach
    void setUp() {
        filter = new ServiceAuthenticationFilter();
        ReflectionTestUtils.setField(filter, "expectedApiKey", VALID_API_KEY);
        ReflectionTestUtils.setField(filter, "allowedServicesConfig", ALLOWED_SERVICES);
    }
    
    @Test
    void shouldAllowPublicPaths() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        
        filter.doFilterInternal(request, response, filterChain);
        
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }
    
    @Test
    void shouldAllowValidServiceAuthentication() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/users/1");
        when(request.getHeader("X-Service-API-Key")).thenReturn(VALID_API_KEY);
        when(request.getHeader("X-Service-Name")).thenReturn("api-gateway");
        
        filter.doFilterInternal(request, response, filterChain);
        
        verify(filterChain).doFilter(request, response);
        verify(request).setAttribute("authenticatedService", "api-gateway");
    }
    
    @Test
    void shouldRejectInvalidApiKey() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/users/1");
        when(request.getHeader("X-Service-API-Key")).thenReturn("invalid-key");
        when(request.getHeader("X-Service-Name")).thenReturn("api-gateway");
        
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        
        filter.doFilterInternal(request, response, filterChain);
        
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }
    
    @Test
    void shouldRejectUnauthorizedService() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/users/1");
        when(request.getHeader("X-Service-API-Key")).thenReturn(VALID_API_KEY);
        when(request.getHeader("X-Service-Name")).thenReturn("unauthorized-service");
        
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        
        filter.doFilterInternal(request, response, filterChain);
        
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }
    
    @Test
    void shouldAllowRequestsWithoutServiceHeaders() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/users/1");
        when(request.getHeader("X-Service-API-Key")).thenReturn(null);
        when(request.getHeader("X-Service-Name")).thenReturn(null);
        
        filter.doFilterInternal(request, response, filterChain);
        
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }
}
