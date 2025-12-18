package com.bookstore.usermanagement.service;

import com.bookstore.usermanagement.dto.JwtResponse;
import com.bookstore.usermanagement.dto.LoginRequest;
import com.bookstore.usermanagement.dto.RefreshTokenRequest;
import com.bookstore.usermanagement.dto.RegisterRequest;
import com.bookstore.usermanagement.entity.Role;
import com.bookstore.usermanagement.entity.User;
import com.bookstore.usermanagement.repository.UserRepository;
import com.bookstore.usermanagement.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Transactional
public class AuthService {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtil.generateJwtToken(authentication);
        String refreshToken = jwtUtil.generateRefreshToken(loginRequest.getUsername());
        
        User user = (User) authentication.getPrincipal();
        
        return new JwtResponse(jwt, refreshToken, user.getId(), user.getUsername(), 
                              user.getEmail(), user.getRoles());
    }
    
    public JwtResponse registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Error: Username is already taken!");
        }
        
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }
        
        // Create new user account
        User user = new User(registerRequest.getUsername(),
                           passwordEncoder.encode(registerRequest.getPassword()),
                           registerRequest.getEmail(),
                           Set.of(Role.USER)); // Default role
        
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        
        User savedUser = userRepository.save(user);
        
        // Generate JWT tokens
        String jwt = jwtUtil.generateTokenFromUsername(savedUser.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(savedUser.getUsername());
        
        return new JwtResponse(jwt, refreshToken, savedUser.getId(), savedUser.getUsername(),
                              savedUser.getEmail(), savedUser.getRoles());
    }
    
    public JwtResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();
        
        if (!jwtUtil.validateJwtToken(refreshToken)) {
            throw new RuntimeException("Refresh token is not valid!");
        }
        
        if (jwtUtil.isTokenExpired(refreshToken)) {
            throw new RuntimeException("Refresh token is expired!");
        }
        
        String username = jwtUtil.getUsernameFromJwtToken(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        
        String newJwt = jwtUtil.generateTokenFromUsername(username);
        String newRefreshToken = jwtUtil.generateRefreshToken(username);
        
        return new JwtResponse(newJwt, newRefreshToken, user.getId(), user.getUsername(),
                              user.getEmail(), user.getRoles());
    }
}