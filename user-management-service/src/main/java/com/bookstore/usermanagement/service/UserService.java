package com.bookstore.usermanagement.service;

import com.bookstore.usermanagement.dto.UserSearchRequest;
import com.bookstore.usermanagement.dto.UserUpdateRequest;
import com.bookstore.usermanagement.entity.Role;
import com.bookstore.usermanagement.entity.User;
import com.bookstore.usermanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }
    
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }
    
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
    
    public List<User> findAll() {
        return userRepository.findAll();
    }
    
    public User updateUser(Long id, UserUpdateRequest userDetails) {
        User user = findById(id);
        
        if (userDetails.getFirstName() != null) {
            user.setFirstName(userDetails.getFirstName());
        }
        if (userDetails.getLastName() != null) {
            user.setLastName(userDetails.getLastName());
        }
        if (userDetails.getEmail() != null && !userDetails.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(userDetails.getEmail())) {
                throw new RuntimeException("Email is already in use!");
            }
            user.setEmail(userDetails.getEmail());
        }
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
        
        return userRepository.save(user);
    }
    
    public User updateUserRoles(Long id, Set<Role> roles) {
        User user = findById(id);
        user.setRoles(roles);
        return userRepository.save(user);
    }
    
    public User enableUser(Long id) {
        User user = findById(id);
        user.setEnabled(true);
        return userRepository.save(user);
    }
    
    public User disableUser(Long id) {
        User user = findById(id);
        user.setEnabled(false);
        return userRepository.save(user);
    }
    
    public void deleteUser(Long id) {
        User user = findById(id);
        userRepository.delete(user);
    }
    
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    public List<User> searchUsers(UserSearchRequest searchRequest) {
        List<User> users = userRepository.findAll();
        
        if (searchRequest.getUsername() != null && !searchRequest.getUsername().isEmpty()) {
            users = users.stream()
                    .filter(u -> u.getUsername().toLowerCase().contains(searchRequest.getUsername().toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (searchRequest.getEmail() != null && !searchRequest.getEmail().isEmpty()) {
            users = users.stream()
                    .filter(u -> u.getEmail().toLowerCase().contains(searchRequest.getEmail().toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (searchRequest.getFirstName() != null && !searchRequest.getFirstName().isEmpty()) {
            users = users.stream()
                    .filter(u -> u.getFirstName() != null && 
                            u.getFirstName().toLowerCase().contains(searchRequest.getFirstName().toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (searchRequest.getLastName() != null && !searchRequest.getLastName().isEmpty()) {
            users = users.stream()
                    .filter(u -> u.getLastName() != null && 
                            u.getLastName().toLowerCase().contains(searchRequest.getLastName().toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (searchRequest.getRole() != null) {
            users = users.stream()
                    .filter(u -> u.getRoles().contains(searchRequest.getRole()))
                    .collect(Collectors.toList());
        }
        
        if (searchRequest.getEnabled() != null) {
            users = users.stream()
                    .filter(u -> u.getEnabled().equals(searchRequest.getEnabled()))
                    .collect(Collectors.toList());
        }
        
        return users;
    }
    
    public List<User> findByRole(Role role) {
        return userRepository.findByRole(role);
    }
    
    public List<User> findByEnabled(Boolean enabled) {
        return userRepository.findByEnabled(enabled);
    }
}