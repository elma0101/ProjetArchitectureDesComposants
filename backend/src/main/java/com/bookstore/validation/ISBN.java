package com.bookstore.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation for ISBN validation
 */
@Documented
@Constraint(validatedBy = ISBNValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ISBN {
    
    String message() default "Invalid ISBN format. Must be a valid ISBN-10 or ISBN-13";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}