package com.bookstore.usermanagement.service;

import com.bookstore.usermanagement.dto.JwtResponse;
import com.bookstore.usermanagement.dto.LoginRequest;
import com.bookstore.usermanagement.dto.RegisterRequest;
import com.bookstore.usermanagement.entity.Role;
import com.bookstore.usermanagement.entity.User;
import com.bookstore.usermanagement.repository.UserRepository;
import com.bookstore.usermanagement.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    
    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @Mock
    private Authentication authentication;
    
    @InjectMocks
    private AuthService authService;
    
    private User testUser;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;
    
    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "encodedPassword", "test@example.com", Set.of(Role.USER));
        testUser.setId(1L);
        
        loginRequest = new LoginRequest("testuser", "password");
        registerRequest = new RegisterRequest("newuser", "password", "new@example.com", "John", "Doe");
    }
    
    @Test
    void authenticateUser_Success() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(jwtUtil.generateJwtToken(authentication)).thenReturn("jwt-token");
        when(jwtUtil.generateRefreshToken(anyString())).thenReturn("refresh-token");
        
        // When
        JwtResponse response = authService.authenticateUser(loginRequest);
        
        // Then
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals(testUser.getId(), response.getId());
        assertEquals(testUser.getUsername(), response.getUsername());
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals(testUser.getRoles(), response.getRoles());
    }
    
    @Test
    void registerUser_Success() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateTokenFromUsername(anyString())).thenReturn("jwt-token");
        when(jwtUtil.generateRefreshToken(anyString())).thenReturn("refresh-token");
        
        // When
        JwtResponse response = authService.registerUser(registerRequest);
        
        // Then
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("refresh-token", response.getRefreshToken());
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    void registerUser_UsernameAlreadyExists() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(true);
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> authService.registerUser(registerRequest));
        assertEquals("Error: Username is already taken!", exception.getMessage());
    }
    
    @Test
    void registerUser_EmailAlreadyExists() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> authService.registerUser(registerRequest));
        assertEquals("Error: Email is already in use!", exception.getMessage());
    }
}