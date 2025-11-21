package com.bookstore.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Enhanced email validator with business rules
 */
public class EmailValidator implements ConstraintValidator<ValidEmail, String> {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    private static final Pattern DISPOSABLE_EMAIL_PATTERN = Pattern.compile(
        ".*(10minutemail|guerrillamail|mailinator|tempmail|throwaway).*"
    );
    
    private boolean allowTestEmails;
    private boolean requireDomain;
    
    @Override
    public void initialize(ValidEmail constraintAnnotation) {
        this.allowTestEmails = constraintAnnotation.allowTestEmails();
        this.requireDomain = constraintAnnotation.requireDomain();
    }
    
    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        String cleanEmail = email.trim().toLowerCase();
        
        // Basic format validation
        if (!EMAIL_PATTERN.matcher(cleanEmail).matches()) {
            return false;
        }
        
        // Check for test emails if not allowed
        if (!allowTestEmails && isTestEmail(cleanEmail)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Test or example emails are not allowed")
                   .addConstraintViolation();
            return false;
        }
        
        // Check for disposable email services
        if (DISPOSABLE_EMAIL_PATTERN.matcher(cleanEmail).find()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Disposable email addresses are not allowed")
                   .addConstraintViolation();
            return false;
        }
        
        // Check domain requirement
        if (requireDomain && !hasValidDomain(cleanEmail)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Email must have a valid domain")
                   .addConstraintViolation();
            return false;
        }
        
        return true;
    }
    
    private boolean isTestEmail(String email) {
        return email.contains("test") || 
               email.contains("example") || 
               email.contains("demo") ||
               email.endsWith("@example.com") ||
               email.endsWith("@test.com");
    }
    
    private boolean hasValidDomain(String email) {
        String domain = email.substring(email.indexOf('@') + 1);
        return domain.contains(".") && domain.length() > 3;
    }
}