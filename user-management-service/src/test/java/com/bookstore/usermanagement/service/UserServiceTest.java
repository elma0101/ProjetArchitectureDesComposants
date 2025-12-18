package com.bookstore.usermanagement.service;

import com.bookstore.usermanagement.dto.UserSearchRequest;
import com.bookstore.usermanagement.dto.UserUpdateRequest;
import com.bookstore.usermanagement.entity.Role;
import com.bookstore.usermanagement.entity.User;
import com.bookstore.usermanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserService userService;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPassword("encodedPassword");
        testUser.setEnabled(true);
        testUser.setRoles(Set.of(Role.USER));
    }
    
    @Test
    void testFindById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        User result = userService.findById(1L);
        
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).findById(1L);
    }
    
    @Test
    void testFindById_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> userService.findById(999L));
        verify(userRepository, times(1)).findById(999L);
    }
    
    @Test
    void testFindByUsername_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        User result = userService.findByUsername("testuser");
        
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
    }
    
    @Test
    void testFindAll() {
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);
        
        List<User> result = userService.findAll();
        
        assertEquals(1, result.size());
        verify(userRepository, times(1)).findAll();
    }
    
    @Test
    void testUpdateUser_Success() {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setFirstName("Updated");
        updateRequest.setLastName("Name");
        updateRequest.setEmail("updated@example.com");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        User result = userService.updateUser(1L, updateRequest);
        
        assertNotNull(result);
        assertEquals("Updated", result.getFirstName());
        assertEquals("Name", result.getLastName());
        verify(userRepository, times(1)).save(any(User.class));
    }
    
    @Test
    void testUpdateUser_EmailAlreadyExists() {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setEmail("existing@example.com");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);
        
        assertThrows(RuntimeException.class, () -> userService.updateUser(1L, updateRequest));
    }
    
    @Test
    void testUpdateUser_WithPassword() {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setPassword("newPassword");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        User result = userService.updateUser(1L, updateRequest);
        
        assertNotNull(result);
        verify(passwordEncoder, times(1)).encode("newPassword");
        verify(userRepository, times(1)).save(any(User.class));
    }
    
    @Test
    void testUpdateUserRoles() {
        Set<Role> newRoles = Set.of(Role.ADMIN, Role.USER);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        User result = userService.updateUserRoles(1L, newRoles);
        
        assertNotNull(result);
        verify(userRepository, times(1)).save(any(User.class));
    }
    
    @Test
    void testEnableUser() {
        testUser.setEnabled(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        User result = userService.enableUser(1L);
        
        assertTrue(result.getEnabled());
        verify(userRepository, times(1)).save(any(User.class));
    }
    
    @Test
    void testDisableUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        User result = userService.disableUser(1L);
        
        assertFalse(result.getEnabled());
        verify(userRepository, times(1)).save(any(User.class));
    }
    
    @Test
    void testDeleteUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(any(User.class));
        
        userService.deleteUser(1L);
        
        verify(userRepository, times(1)).delete(any(User.class));
    }
    
    @Test
    void testSearchUsers_ByUsername() {
        UserSearchRequest searchRequest = new UserSearchRequest();
        searchRequest.setUsername("test");
        
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));
        
        List<User> result = userService.searchUsers(searchRequest);
        
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
    }
    
    @Test
    void testSearchUsers_ByEmail() {
        UserSearchRequest searchRequest = new UserSearchRequest();
        searchRequest.setEmail("test@");
        
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));
        
        List<User> result = userService.searchUsers(searchRequest);
        
        assertEquals(1, result.size());
        assertEquals("test@example.com", result.get(0).getEmail());
    }
    
    @Test
    void testSearchUsers_ByRole() {
        UserSearchRequest searchRequest = new UserSearchRequest();
        searchRequest.setRole(Role.USER);
        
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));
        
        List<User> result = userService.searchUsers(searchRequest);
        
        assertEquals(1, result.size());
        assertTrue(result.get(0).getRoles().contains(Role.USER));
    }
    
    @Test
    void testSearchUsers_ByEnabled() {
        UserSearchRequest searchRequest = new UserSearchRequest();
        searchRequest.setEnabled(true);
        
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));
        
        List<User> result = userService.searchUsers(searchRequest);
        
        assertEquals(1, result.size());
        assertTrue(result.get(0).getEnabled());
    }
    
    @Test
    void testSearchUsers_MultipleFilters() {
        UserSearchRequest searchRequest = new UserSearchRequest();
        searchRequest.setUsername("test");
        searchRequest.setEnabled(true);
        searchRequest.setRole(Role.USER);
        
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));
        
        List<User> result = userService.searchUsers(searchRequest);
        
        assertEquals(1, result.size());
    }
    
    @Test
    void testSearchUsers_NoMatch() {
        UserSearchRequest searchRequest = new UserSearchRequest();
        searchRequest.setUsername("nonexistent");
        
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));
        
        List<User> result = userService.searchUsers(searchRequest);
        
        assertEquals(0, result.size());
    }
    
    @Test
    void testFindByRole() {
        when(userRepository.findByRole(Role.USER)).thenReturn(Arrays.asList(testUser));
        
        List<User> result = userService.findByRole(Role.USER);
        
        assertEquals(1, result.size());
        verify(userRepository, times(1)).findByRole(Role.USER);
    }
    
    @Test
    void testFindByEnabled() {
        when(userRepository.findByEnabled(true)).thenReturn(Arrays.asList(testUser));
        
        List<User> result = userService.findByEnabled(true);
        
        assertEquals(1, result.size());
        verify(userRepository, times(1)).findByEnabled(true);
    }
    
    @Test
    void testExistsByUsername() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        
        boolean result = userService.existsByUsername("testuser");
        
        assertTrue(result);
        verify(userRepository, times(1)).existsByUsername("testuser");
    }
    
    @Test
    void testExistsByEmail() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        
        boolean result = userService.existsByEmail("test@example.com");
        
        assertTrue(result);
        verify(userRepository, times(1)).existsByEmail("test@example.com");
    }
}
