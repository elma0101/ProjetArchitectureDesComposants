package com.bookstore.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

/**
 * Validator for year validation with business rules
 */
public class YearValidator implements ConstraintValidator<ValidYear, Integer> {
    
    private int min;
    private int max;
    private boolean allowFuture;
    
    @Override
    public void initialize(ValidYear constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
        this.allowFuture = constraintAnnotation.allowFuture();
    }
    
    @Override
    public boolean isValid(Integer year, ConstraintValidatorContext context) {
        if (year == null) {
            return true; // Let @NotNull handle null validation
        }
        
        int currentYear = LocalDate.now().getYear();
        
        // Check minimum and maximum bounds
        if (year < min || year > max) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                String.format("Year must be between %d and %d", min, max))
                   .addConstraintViolation();
            return false;
        }
        
        // Check future year restriction
        if (!allowFuture && year > currentYear + 1) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Publication year cannot be more than one year in the future")
                   .addConstraintViolation();
            return false;
        }
        
        return true;
    }
}