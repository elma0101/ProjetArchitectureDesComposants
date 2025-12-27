package com.bookstore.catalog.validation;

import com.bookstore.catalog.entity.Book;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for copy count validation
 * Ensures that available copies does not exceed total copies
 */
public class CopyCountValidator implements ConstraintValidator<ValidCopyCount, Book> {
    
    @Override
    public void initialize(ValidCopyCount constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(Book book, ConstraintValidatorContext context) {
        if (book == null) {
            return true;
        }
        
        Integer availableCopies = book.getAvailableCopies();
        Integer totalCopies = book.getTotalCopies();
        
        if (availableCopies == null || totalCopies == null) {
            return true; // Let other validators handle null checks
        }
        
        return availableCopies <= totalCopies;
    }
}
