package com.bookstore.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;
import java.time.LocalDate;

/**
 * Validator for date range validation
 */
public class DateRangeValidator implements ConstraintValidator<ValidDateRange, Object> {
    
    private String startDateField;
    private String endDateField;
    
    @Override
    public void initialize(ValidDateRange constraintAnnotation) {
        this.startDateField = constraintAnnotation.startDateField();
        this.endDateField = constraintAnnotation.endDateField();
    }
    
    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        if (object == null) {
            return true;
        }
        
        try {
            LocalDate startDate = getFieldValue(object, startDateField);
            LocalDate endDate = getFieldValue(object, endDateField);
            
            if (startDate == null || endDate == null) {
                return true; // Let other validators handle null checks
            }
            
            return !startDate.isAfter(endDate);
            
        } catch (Exception e) {
            return false;
        }
    }
    
    private LocalDate getFieldValue(Object object, String fieldName) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (LocalDate) field.get(object);
    }
}