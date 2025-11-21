package com.bookstore.security;

import com.bookstore.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuditingAspect {
    
    @Autowired
    private AuditService auditService;
    
    @AfterReturning(pointcut = "execution(* com.bookstore.controller.*.create*(..))", returning = "result")
    public void auditCreate(JoinPoint joinPoint, Object result) {
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            String resourceType = extractResourceType(joinPoint);
            auditService.logAction("CREATE", resourceType, null, request, 201, 
                                 "Resource created: " + joinPoint.getSignature().getName());
        }
    }
    
    @AfterReturning(pointcut = "execution(* com.bookstore.controller.*.update*(..))", returning = "result")
    public void auditUpdate(JoinPoint joinPoint, Object result) {
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            String resourceType = extractResourceType(joinPoint);
            auditService.logAction("UPDATE", resourceType, null, request, 200, 
                                 "Resource updated: " + joinPoint.getSignature().getName());
        }
    }
    
    @AfterReturning(pointcut = "execution(* com.bookstore.controller.*.delete*(..))", returning = "result")
    public void auditDelete(JoinPoint joinPoint, Object result) {
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            String resourceType = extractResourceType(joinPoint);
            auditService.logAction("DELETE", resourceType, null, request, 200, 
                                 "Resource deleted: " + joinPoint.getSignature().getName());
        }
    }
    
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
    
    private String extractResourceType(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        return className.replace("Controller", "").toUpperCase();
    }
}