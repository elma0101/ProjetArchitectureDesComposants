package com.bookstore.catalog.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for ISBN validation
 * Supports both ISBN-10 and ISBN-13 formats
 */
public class ISBNValidator implements ConstraintValidator<ISBN, String> {
    
    @Override
    public void initialize(ISBN constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(String isbn, ConstraintValidatorContext context) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return false;
        }
        
        // Remove hyphens and spaces
        String cleanIsbn = isbn.replaceAll("[\\s-]", "");
        
        // Check if it's ISBN-10 or ISBN-13
        if (cleanIsbn.length() == 10) {
            return isValidISBN10(cleanIsbn);
        } else if (cleanIsbn.length() == 13) {
            return isValidISBN13(cleanIsbn);
        }
        
        return false;
    }
    
    /**
     * Validate ISBN-10 format
     * ISBN-10 uses a weighted sum with modulo 11 check digit
     */
    private boolean isValidISBN10(String isbn) {
        if (!isbn.matches("^[0-9]{9}[0-9X]$")) {
            return false;
        }
        
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (isbn.charAt(i) - '0') * (10 - i);
        }
        
        char checkChar = isbn.charAt(9);
        int checkDigit = (checkChar == 'X') ? 10 : (checkChar - '0');
        
        return (sum + checkDigit) % 11 == 0;
    }
    
    /**
     * Validate ISBN-13 format
     * ISBN-13 uses alternating weights of 1 and 3 with modulo 10 check digit
     */
    private boolean isValidISBN13(String isbn) {
        if (!isbn.matches("^[0-9]{13}$")) {
            return false;
        }
        
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = isbn.charAt(i) - '0';
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        
        int checkDigit = isbn.charAt(12) - '0';
        int calculatedCheck = (10 - (sum % 10)) % 10;
        
        return checkDigit == calculatedCheck;
    }
}
