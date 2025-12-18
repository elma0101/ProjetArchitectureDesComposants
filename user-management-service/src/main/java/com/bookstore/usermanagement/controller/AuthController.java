package com.bookstore.usermanagement.controller;

import com.bookstore.usermanagement.dto.JwtResponse;
import com.bookstore.usermanagement.dto.LoginRequest;
import com.bookstore.usermanagement.dto.RefreshTokenRequest;
import com.bookstore.usermanagement.dto.RegisterRequest;
import com.bookstore.usermanagement.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
            return ResponseEntity.ok(jwtResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<JwtResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            JwtResponse jwtResponse = authService.registerUser(registerRequest);
            return ResponseEntity.ok(jwtResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        try {
            JwtResponse jwtResponse = authService.refreshToken(refreshTokenRequest);
            return ResponseEntity.ok(jwtResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        // In a stateless JWT implementation, logout is typically handled client-side
        // by removing the token from storage. Server-side token blacklisting could be
        // implemented here if needed.
        return ResponseEntity.ok().build();
    }
}