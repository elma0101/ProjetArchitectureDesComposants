package com.bookstore.security;

import com.bookstore.entity.AuditLog;
import com.bookstore.entity.Role;
import com.bookstore.entity.User;
import com.bookstore.repository.AuditLogRepository;
import com.bookstore.repository.UserRepository;
import com.bookstore.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AuditingTest {
    
    @Autowired
    private AuditService auditService;
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User("testuser", passwordEncoder.encode("password"), 
                          "test@test.com", Set.of(Role.USER));
        userRepository.save(testUser);
        
        // Set up security context
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(testUser.getUsername(), null, testUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    
    @Test
    void testAuditLogCreation() {
        // Test audit log creation
        auditService.logAction("CREATE", "BOOK", "123", "Book created successfully");
        
        Page<AuditLog> logs = auditService.getAuditLogs(PageRequest.of(0, 10));
        assertFalse(logs.isEmpty());
        
        AuditLog log = logs.getContent().get(0);
        assertEquals("CREATE", log.getAction());
        assertEquals("BOOK", log.getResourceType());
        assertEquals("123", log.getResourceId());
        assertEquals("testuser", log.getUsername());
        assertNotNull(log.getTimestamp());
    }
    
    @Test
    void testAuditLogsByUsername() {
        // Create audit logs for different users
        auditService.logAction("CREATE", "BOOK", "1", "Book created");
        auditService.logAction("UPDATE", "BOOK", "1", "Book updated");
        
        Page<AuditLog> userLogs = auditService.getAuditLogsByUsername("testuser", PageRequest.of(0, 10));
        assertEquals(2, userLogs.getTotalElements());
        
        userLogs.getContent().forEach(log -> 
            assertEquals("testuser", log.getUsername())
        );
    }
    
    @Test
    void testAuditLogsByAction() {
        auditService.logAction("CREATE", "BOOK", "1", "Book created");
        auditService.logAction("CREATE", "AUTHOR", "1", "Author created");
        auditService.logAction("UPDATE", "BOOK", "1", "Book updated");
        
        Page<AuditLog> createLogs = auditService.getAuditLogsByAction("CREATE", PageRequest.of(0, 10));
        assertEquals(2, createLogs.getTotalElements());
        
        createLogs.getContent().forEach(log -> 
            assertEquals("CREATE", log.getAction())
        );
    }
    
    @Test
    void testAuditLogsByDateRange() {
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(1);
        
        auditService.logAction("CREATE", "BOOK", "1", "Book created");
        
        Page<AuditLog> logs = auditService.getAuditLogsByDateRange(start, end, PageRequest.of(0, 10));
        assertFalse(logs.isEmpty());
        
        logs.getContent().forEach(log -> {
            assertTrue(log.getTimestamp().isAfter(start));
            assertTrue(log.getTimestamp().isBefore(end));
        });
    }
    
    @Test
    void testAuditLogPersistence() {
        auditService.logAction("DELETE", "BOOK", "999", "Book deleted");
        
        // Verify the log was persisted
        Page<AuditLog> logs = auditLogRepository.findAll(PageRequest.of(0, 10));
        assertFalse(logs.isEmpty());
        
        AuditLog persistedLog = logs.getContent().stream()
            .filter(log -> "DELETE".equals(log.getAction()))
            .findFirst()
            .orElse(null);
        
        assertNotNull(persistedLog);
        assertEquals("DELETE", persistedLog.getAction());
        assertEquals("BOOK", persistedLog.getResourceType());
        assertEquals("999", persistedLog.getResourceId());
    }
}