package com.bookstore.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for sending email notifications
 * This is a mock implementation for demonstration purposes
 * In a real application, this would integrate with an email service like SendGrid, AWS SES, etc.
 */
@Service
public class EmailNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);
    
    /**
     * Send email notification to borrower
     */
    public void sendNotification(String email, String name, String subject, String message) {
        // Mock implementation - in real application, this would send actual emails
        logger.info("MOCK EMAIL NOTIFICATION:");
        logger.info("To: {} ({})", email, name);
        logger.info("Subject: {}", subject);
        logger.info("Message: {}", message);
        logger.info("--- END EMAIL ---");
        
        // Simulate email sending delay
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Send bulk notifications
     */
    public void sendBulkNotifications(java.util.List<EmailNotification> notifications) {
        for (EmailNotification notification : notifications) {
            sendNotification(
                notification.getEmail(),
                notification.getName(),
                notification.getSubject(),
                notification.getMessage()
            );
        }
    }
    
    /**
     * Check if email service is available
     */
    public boolean isServiceAvailable() {
        // Mock implementation - always return true
        return true;
    }
    
    /**
     * Email notification data class
     */
    public static class EmailNotification {
        private String email;
        private String name;
        private String subject;
        private String message;
        
        public EmailNotification(String email, String name, String subject, String message) {
            this.email = email;
            this.name = name;
            this.subject = subject;
            this.message = message;
        }
        
        // Getters
        public String getEmail() { return email; }
        public String getName() { return name; }
        public String getSubject() { return subject; }
        public String getMessage() { return message; }
    }
}