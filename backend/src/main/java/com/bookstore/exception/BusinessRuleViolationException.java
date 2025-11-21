package com.bookstore.exception;

/**
 * Exception thrown when business rules are violated
 */
public class BusinessRuleViolationException extends RuntimeException {
    
    private final String ruleCode;
    
    public BusinessRuleViolationException(String message) {
        super(message);
        this.ruleCode = null;
    }
    
    public BusinessRuleViolationException(String ruleCode, String message) {
        super(message);
        this.ruleCode = ruleCode;
    }
    
    public BusinessRuleViolationException(String message, Throwable cause) {
        super(message, cause);
        this.ruleCode = null;
    }
    
    public String getRuleCode() {
        return ruleCode;
    }
}