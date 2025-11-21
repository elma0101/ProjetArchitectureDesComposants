package com.bookstore.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Utility class for common validation operations
 */
public class ValidationUtils {
    
    private static final Pattern SAFE_STRING_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s\\-_.,!?()]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9\\s\\-()]{10,15}$");
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");
    
    /**
     * Validates an object and returns field errors as a map
     */
    public static <T> Map<String, String> validateObject(Validator validator, T object, Class<?>... groups) {
        Set<ConstraintViolation<T>> violations = validator.validate(object, groups);
        Map<String, String> errors = new HashMap<>();
        
        for (ConstraintViolation<T> violation : violations) {
            String fieldName = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errors.put(fieldName, message);
        }
        
        return errors;
    }
    
    /**
     * Checks if a string contains only safe characters (no SQL injection, XSS)
     */
    public static boolean isSafeString(String input) {
        if (input == null || input.trim().isEmpty()) {
            return true;
        }
        
        String cleanInput = input.trim();
        
        // Check for potential SQL injection patterns
        if (containsSqlInjectionPatterns(cleanInput)) {
            return false;
        }
        
        // Check for potential XSS patterns
        if (containsXssPatterns(cleanInput)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Validates phone number format
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        
        return PHONE_PATTERN.matcher(phoneNumber.trim()).matches();
    }
    
    /**
     * Validates that a string contains only alphanumeric characters
     */
    public static boolean isAlphanumeric(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        
        return ALPHANUMERIC_PATTERN.matcher(input.trim()).matches();
    }
    
    /**
     * Validates string length within bounds
     */
    public static boolean isValidLength(String input, int minLength, int maxLength) {
        if (input == null) {
            return minLength == 0;
        }
        
        int length = input.trim().length();
        return length >= minLength && length <= maxLength;
    }
    
    /**
     * Validates that a number is within specified range
     */
    public static boolean isInRange(Number value, Number min, Number max) {
        if (value == null) {
            return false;
        }
        
        double doubleValue = value.doubleValue();
        double minValue = min != null ? min.doubleValue() : Double.MIN_VALUE;
        double maxValue = max != null ? max.doubleValue() : Double.MAX_VALUE;
        
        return doubleValue >= minValue && doubleValue <= maxValue;
    }
    
    /**
     * Sanitizes input string by removing potentially harmful characters
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        return input.trim()
                   .replaceAll("<", "&lt;")
                   .replaceAll(">", "&gt;")
                   .replaceAll("\"", "&quot;")
                   .replaceAll("'", "&#x27;")
                   .replaceAll("&", "&amp;");
    }
    
    /**
     * Validates that a string is not empty or whitespace only
     */
    public static boolean isNotBlank(String input) {
        return input != null && !input.trim().isEmpty();
    }
    
    /**
     * Validates pagination parameters
     */
    public static boolean isValidPaginationParams(Integer page, Integer size) {
        if (page != null && page < 0) {
            return false;
        }
        
        if (size != null && (size < 1 || size > 100)) {
            return false;
        }
        
        return true;
    }
    
    private static boolean containsSqlInjectionPatterns(String input) {
        String lowerInput = input.toLowerCase();
        String[] sqlPatterns = {
            "select", "insert", "update", "delete", "drop", "create", "alter",
            "union", "exec", "execute", "script", "--", "/*", "*/", "xp_",
            "sp_", "0x", "char(", "ascii(", "substring(", "waitfor"
        };
        
        for (String pattern : sqlPatterns) {
            if (lowerInput.contains(pattern)) {
                return true;
            }
        }
        
        return false;
    }
    
    private static boolean containsXssPatterns(String input) {
        String lowerInput = input.toLowerCase();
        String[] xssPatterns = {
            "<script", "</script>", "javascript:", "onload=", "onerror=",
            "onclick=", "onmouseover=", "onfocus=", "onblur=", "eval(",
            "expression(", "vbscript:", "data:"
        };
        
        for (String pattern : xssPatterns) {
            if (lowerInput.contains(pattern)) {
                return true;
            }
        }
        
        return false;
    }
}