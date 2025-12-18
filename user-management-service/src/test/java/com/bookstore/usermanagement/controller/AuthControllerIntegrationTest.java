package com.bookstore.usermanagement.controller;

import com.bookstore.usermanagement.dto.JwtResponse;
import com.bookstore.usermanagement.dto.LoginRequest;
import com.bookstore.usermanagement.dto.RegisterRequest;
import com.bookstore.usermanagement.entity.Role;
import com.bookstore.usermanagement.entity.User;
import com.bookstore.usermanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AuthControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        
        testUser = new User("testuser", passwordEncoder.encode("password"), 
                           "test@example.com", Set.of(Role.USER));
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        userRepository.save(testUser);
    }
    
    @Test
    void login_Success() {
        LoginRequest loginRequest = new LoginRequest("testuser", "password");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, headers);
        
        ResponseEntity<JwtResponse> response = restTemplate.postForEntity("/api/auth/login", request, JwtResponse.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getToken());
        assertNotNull(response.getBody().getRefreshToken());
        assertEquals("testuser", response.getBody().getUsername());
        assertEquals("test@example.com", response.getBody().getEmail());
    }
    
    @Test
    void login_InvalidCredentials() {
        LoginRequest loginRequest = new LoginRequest("testuser", "wrongpassword");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/login", request, String.class);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    
    @Test
    void register_Success() {
        RegisterRequest registerRequest = new RegisterRequest("newuser", "password", 
                                                            "new@example.com", "New", "User");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegisterRequest> request = new HttpEntity<>(registerRequest, headers);
        
        ResponseEntity<JwtResponse> response = restTemplate.postForEntity("/api/auth/register", request, JwtResponse.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getToken());
        assertNotNull(response.getBody().getRefreshToken());
        assertEquals("newuser", response.getBody().getUsername());
        assertEquals("new@example.com", response.getBody().getEmail());
    }
    
    @Test
    void register_UsernameAlreadyExists() {
        RegisterRequest registerRequest = new RegisterRequest("testuser", "password", 
                                                            "different@example.com", "Test", "User");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegisterRequest> request = new HttpEntity<>(registerRequest, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/register", request, String.class);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    
    @Test
    void register_EmailAlreadyExists() {
        RegisterRequest registerRequest = new RegisterRequest("differentuser", "password", 
                                                            "test@example.com", "Test", "User");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegisterRequest> request = new HttpEntity<>(registerRequest, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/register", request, String.class);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    
    @Test
    void register_InvalidInput() {
        RegisterRequest registerRequest = new RegisterRequest("", "", "", "", "");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegisterRequest> request = new HttpEntity<>(registerRequest, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/register", request, String.class);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}