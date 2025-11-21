package com.bookstore.service;

import com.bookstore.entity.AuditLog;
import com.bookstore.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditService {
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    public void logAction(String action, String resourceType, String resourceId, 
                         HttpServletRequest request, Integer statusCode, String details) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "anonymous";
        String userId = authentication != null ? authentication.getName() : null;
        
        String ipAddress = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");
        String requestMethod = request.getMethod();
        String requestUrl = request.getRequestURL().toString();
        
        AuditLog auditLog = new AuditLog(userId, username, action, resourceType, resourceId,
                                       ipAddress, userAgent, requestMethod, requestUrl, statusCode, details);
        
        auditLogRepository.save(auditLog);
    }
    
    public void logAction(String action, String resourceType, String resourceId, String details) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "anonymous";
        String userId = authentication != null ? authentication.getName() : null;
        
        AuditLog auditLog = new AuditLog(userId, username, action, resourceType, resourceId,
                                       null, null, null, null, null, details);
        
        auditLogRepository.save(auditLog);
    }
    
    public Page<AuditLog> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }
    
    public Page<AuditLog> getAuditLogsByUsername(String username, Pageable pageable) {
        return auditLogRepository.findByUsernameOrderByTimestampDesc(username, pageable);
    }
    
    public Page<AuditLog> getAuditLogsByAction(String action, Pageable pageable) {
        return auditLogRepository.findByActionOrderByTimestampDesc(action, pageable);
    }
    
    public Page<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return auditLogRepository.findByTimestampBetween(startDate, endDate, pageable);
    }
    
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}