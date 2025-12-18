package com.bookstore.usermanagement.controller;

import com.bookstore.usermanagement.dto.RoleAssignmentRequest;
import com.bookstore.usermanagement.dto.UserSearchRequest;
import com.bookstore.usermanagement.dto.UserUpdateRequest;
import com.bookstore.usermanagement.entity.Role;
import com.bookstore.usermanagement.entity.User;
import com.bookstore.usermanagement.repository.UserRepository;
import com.bookstore.usermanagement.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private String adminToken;
    private String userToken;
    private User adminUser;
    private User regularUser;
    
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        
        // Create admin user
        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setEmail("admin@example.com");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setRoles(Set.of(Role.ADMIN));
        adminUser.setEnabled(true);
        adminUser = userRepository.save(adminUser);
        
        // Create regular user
        regularUser = new User();
        regularUser.setUsername("user");
        regularUser.setPassword(passwordEncoder.encode("user123"));
        regularUser.setEmail("user@example.com");
        regularUser.setFirstName("Regular");
        regularUser.setLastName("User");
        regularUser.setRoles(Set.of(Role.USER));
        regularUser.setEnabled(true);
        regularUser = userRepository.save(regularUser);
        
        // Generate tokens
        adminToken = jwtUtil.generateTokenFromUsername(adminUser.getUsername());
        userToken = jwtUtil.generateTokenFromUsername(regularUser.getUsername());
    }
    
    @Test
    void testGetCurrentUser() throws Exception {
        mockMvc.perform(get("/api/users/profile")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user"))
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }
    
    @Test
    void testGetUserById_AsAdmin() throws Exception {
        mockMvc.perform(get("/api/users/" + regularUser.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user"))
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }
    
    @Test
    void testGetUserById_AsRegularUser_Forbidden() throws Exception {
        mockMvc.perform(get("/api/users/" + adminUser.getId())
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }
    
    @Test
    void testGetAllUsers_AsAdmin() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].username", containsInAnyOrder("admin", "user")));
    }
    
    @Test
    void testSearchUsers_ByUsername() throws Exception {
        UserSearchRequest searchRequest = new UserSearchRequest();
        searchRequest.setUsername("admin");
        
        mockMvc.perform(post("/api/users/search")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username").value("admin"));
    }
    
    @Test
    void testSearchUsers_ByEmail() throws Exception {
        UserSearchRequest searchRequest = new UserSearchRequest();
        searchRequest.setEmail("user@");
        
        mockMvc.perform(post("/api/users/search")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email").value("user@example.com"));
    }
    
    @Test
    void testSearchUsers_ByRole() throws Exception {
        UserSearchRequest searchRequest = new UserSearchRequest();
        searchRequest.setRole(Role.ADMIN);
        
        mockMvc.perform(post("/api/users/search")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username").value("admin"));
    }
    
    @Test
    void testSearchUsers_ByEnabled() throws Exception {
        UserSearchRequest searchRequest = new UserSearchRequest();
        searchRequest.setEnabled(true);
        
        mockMvc.perform(post("/api/users/search")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
    
    @Test
    void testGetUsersByRole() throws Exception {
        mockMvc.perform(get("/api/users/role/USER")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username").value("user"));
    }
    
    @Test
    void testGetUsersByEnabled() throws Exception {
        mockMvc.perform(get("/api/users/enabled/true")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
    
    @Test
    void testUpdateUser_AsOwner() throws Exception {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setFirstName("Updated");
        updateRequest.setLastName("Name");
        
        mockMvc.perform(put("/api/users/" + regularUser.getId())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Name"));
    }
    
    @Test
    void testUpdateUser_AsAdmin() throws Exception {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setFirstName("AdminUpdated");
        
        mockMvc.perform(put("/api/users/" + regularUser.getId())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("AdminUpdated"));
    }
    
    @Test
    void testUpdateUser_EmailAlreadyExists() throws Exception {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setEmail("admin@example.com");
        
        mockMvc.perform(put("/api/users/" + regularUser.getId())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isInternalServerError());
    }
    
    @Test
    void testUpdateUserRoles_AsAdmin() throws Exception {
        RoleAssignmentRequest roleRequest = new RoleAssignmentRequest();
        roleRequest.setRoles(Set.of(Role.USER, Role.LIBRARIAN));
        
        mockMvc.perform(put("/api/users/" + regularUser.getId() + "/roles")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles", hasSize(2)));
    }
    
    @Test
    void testUpdateUserRoles_AsRegularUser_Forbidden() throws Exception {
        RoleAssignmentRequest roleRequest = new RoleAssignmentRequest();
        roleRequest.setRoles(Set.of(Role.ADMIN));
        
        mockMvc.perform(put("/api/users/" + regularUser.getId() + "/roles")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleRequest)))
                .andExpect(status().isForbidden());
    }
    
    @Test
    void testEnableUser_AsAdmin() throws Exception {
        mockMvc.perform(put("/api/users/" + regularUser.getId() + "/enable")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
    }
    
    @Test
    void testDisableUser_AsAdmin() throws Exception {
        mockMvc.perform(put("/api/users/" + regularUser.getId() + "/disable")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));
    }
    
    @Test
    void testDeleteUser_AsAdmin() throws Exception {
        mockMvc.perform(delete("/api/users/" + regularUser.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
        
        // Verify user is deleted
        mockMvc.perform(get("/api/users/" + regularUser.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isInternalServerError());
    }
    
    @Test
    void testDeleteUser_AsRegularUser_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/users/" + adminUser.getId())
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }
    
    @Test
    void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }
}
