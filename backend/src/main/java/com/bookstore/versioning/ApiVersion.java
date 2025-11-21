package com.bookstore.versioning;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify API version for controllers and methods
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiVersion {
    String value() default "1.0";
    boolean deprecated() default false;
    String deprecatedSince() default "";
    String migrationGuide() default "";
}