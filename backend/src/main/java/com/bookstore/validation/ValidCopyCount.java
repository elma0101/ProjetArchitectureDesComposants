package com.bookstore.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation for validating that available copies <= total copies
 */
@Documented
@Constraint(validatedBy = CopyCountValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCopyCount {
    
    String message() default "Available copies cannot exceed total copies";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}