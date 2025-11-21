package com.bookstore.aspect;

import com.bookstore.service.MonitoringService;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Aspect for automatic monitoring of service methods
 */
@Aspect
@Component
public class MonitoringAspect {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringAspect.class);
    
    private final MonitoringService monitoringService;

    @Autowired
    public MonitoringAspect(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    /**
     * Monitor all service layer methods
     */
    @Around("execution(* com.bookstore.service.*.*(..))")
    public Object monitorServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String fullMethodName = className + "." + methodName;
        
        Timer.Sample sample = monitoringService.startDatabaseTimer();
        
        MDC.put("service", className);
        MDC.put("method", methodName);
        MDC.put("operation", "service_call");
        
        try {
            logger.debug("Entering service method: {}", fullMethodName);
            Object result = joinPoint.proceed();
            logger.debug("Exiting service method: {}", fullMethodName);
            
            monitoringService.recordDatabaseTime(sample, methodName, className);
            return result;
            
        } catch (Exception e) {
            logger.error("Error in service method: {}", fullMethodName, e);
            monitoringService.recordApiError(fullMethodName, e.getClass().getSimpleName(), 
                                           e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    /**
     * Monitor repository layer methods for database operations
     */
    @Around("execution(* com.bookstore.repository.*.*(..))")
    public Object monitorRepositoryMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String fullMethodName = className + "." + methodName;
        
        Timer.Sample sample = monitoringService.startDatabaseTimer();
        
        MDC.put("repository", className);
        MDC.put("method", methodName);
        MDC.put("operation", "database_operation");
        
        try {
            logger.debug("Entering repository method: {}", fullMethodName);
            Object result = joinPoint.proceed();
            logger.debug("Exiting repository method: {}", fullMethodName);
            
            // Extract table name from repository class name
            String tableName = className.replace("Repository", "").toLowerCase();
            monitoringService.recordDatabaseTime(sample, methodName, tableName);
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error in repository method: {}", fullMethodName, e);
            monitoringService.recordApiError(fullMethodName, e.getClass().getSimpleName(), 
                                           e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    /**
     * Monitor controller layer methods for API calls
     */
    @Around("execution(* com.bookstore.controller.*.*(..))")
    public Object monitorControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String fullMethodName = className + "." + methodName;
        
        MDC.put("controller", className);
        MDC.put("method", methodName);
        MDC.put("operation", "api_call");
        
        try {
            logger.debug("Entering controller method: {}", fullMethodName);
            Object result = joinPoint.proceed();
            logger.debug("Exiting controller method: {}", fullMethodName);
            return result;
            
        } catch (Exception e) {
            logger.error("Error in controller method: {}", fullMethodName, e);
            monitoringService.recordApiError(fullMethodName, e.getClass().getSimpleName(), 
                                           e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    /**
     * Monitor external service calls
     */
    @Around("execution(* com.bookstore.client.*.*(..))")
    public Object monitorExternalServiceCalls(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String fullMethodName = className + "." + methodName;
        
        Timer.Sample sample = Timer.start();
        
        MDC.put("client", className);
        MDC.put("method", methodName);
        MDC.put("operation", "external_service_call");
        
        try {
            logger.debug("Calling external service: {}", fullMethodName);
            Object result = joinPoint.proceed();
            logger.debug("External service call completed: {}", fullMethodName);
            
            long duration = sample.stop(Timer.builder("bookstore.external.calls")
                    .tag("client", className)
                    .tag("method", methodName)
                    .register(monitoringService.getMeterRegistry()));
            
            logger.info("External service call duration: {}ms for {}", 
                       duration / 1_000_000, fullMethodName);
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error in external service call: {}", fullMethodName, e);
            monitoringService.recordApiError(fullMethodName, e.getClass().getSimpleName(), 
                                           e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }
}