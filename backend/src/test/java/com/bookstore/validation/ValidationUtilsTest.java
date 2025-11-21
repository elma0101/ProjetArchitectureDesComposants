package com.bookstore.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ValidationUtilsTest {

    @Mock
    private Validator validator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldValidateObjectAndReturnErrors() {
        // Given
        Object testObject = new Object();
        Set<ConstraintViolation<Object>> violations = new HashSet<>();
        
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));
        when(violation.getPropertyPath().toString()).thenReturn("title");
        when(violation.getMessage()).thenReturn("Title is required");
        violations.add(violation);
        
        when(validator.validate(any(), any(Class[].class))).thenReturn(violations);

        // When
        Map<String, String> errors = ValidationUtils.validateObject(validator, testObject);

        // Then
        assertThat(errors).hasSize(1);
        assertThat(errors.get("title")).isEqualTo("Title is required");
    }

    @Test
    void shouldReturnEmptyMapForValidObject() {
        // Given
        Object testObject = new Object();
        Set<ConstraintViolation<Object>> violations = new HashSet<>();
        when(validator.validate(any(), any(Class[].class))).thenReturn(violations);

        // When
        Map<String, String> errors = ValidationUtils.validateObject(validator, testObject);

        // Then
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldValidateSafeString() {
        // Given & When & Then
        assertThat(ValidationUtils.isSafeString("Hello World")).isTrue();
        assertThat(ValidationUtils.isSafeString("Test123")).isTrue();
        assertThat(ValidationUtils.isSafeString("")).isTrue();
        assertThat(ValidationUtils.isSafeString(null)).isTrue();
    }

    @Test
    void shouldRejectUnsafeStrings() {
        // Given & When & Then
        assertThat(ValidationUtils.isSafeString("SELECT * FROM users")).isFalse();
        assertThat(ValidationUtils.isSafeString("<script>alert('xss')</script>")).isFalse();
        assertThat(ValidationUtils.isSafeString("DROP TABLE books")).isFalse();
        assertThat(ValidationUtils.isSafeString("javascript:alert(1)")).isFalse();
    }

    @Test
    void shouldValidatePhoneNumbers() {
        // Given & When & Then
        assertThat(ValidationUtils.isValidPhoneNumber("+1234567890")).isTrue();
        assertThat(ValidationUtils.isValidPhoneNumber("123-456-7890")).isTrue();
        assertThat(ValidationUtils.isValidPhoneNumber("(123) 456-7890")).isTrue();
        assertThat(ValidationUtils.isValidPhoneNumber("1234567890")).isTrue();
        
        assertThat(ValidationUtils.isValidPhoneNumber("123")).isFalse();
        assertThat(ValidationUtils.isValidPhoneNumber("abc123")).isFalse();
        assertThat(ValidationUtils.isValidPhoneNumber("")).isFalse();
        assertThat(ValidationUtils.isValidPhoneNumber(null)).isFalse();
    }

    @Test
    void shouldValidateAlphanumericStrings() {
        // Given & When & Then
        assertThat(ValidationUtils.isAlphanumeric("abc123")).isTrue();
        assertThat(ValidationUtils.isAlphanumeric("ABC")).isTrue();
        assertThat(ValidationUtils.isAlphanumeric("123")).isTrue();
        
        assertThat(ValidationUtils.isAlphanumeric("abc-123")).isFalse();
        assertThat(ValidationUtils.isAlphanumeric("abc 123")).isFalse();
        assertThat(ValidationUtils.isAlphanumeric("")).isFalse();
        assertThat(ValidationUtils.isAlphanumeric(null)).isFalse();
    }

    @Test
    void shouldValidateStringLength() {
        // Given & When & Then
        assertThat(ValidationUtils.isValidLength("hello", 1, 10)).isTrue();
        assertThat(ValidationUtils.isValidLength("hello", 5, 5)).isTrue();
        assertThat(ValidationUtils.isValidLength("", 0, 10)).isTrue();
        
        assertThat(ValidationUtils.isValidLength("hello", 6, 10)).isFalse();
        assertThat(ValidationUtils.isValidLength("hello", 1, 4)).isFalse();
        assertThat(ValidationUtils.isValidLength(null, 1, 10)).isFalse();
        assertThat(ValidationUtils.isValidLength(null, 0, 10)).isTrue();
    }

    @Test
    void shouldValidateNumberRange() {
        // Given & When & Then
        assertThat(ValidationUtils.isInRange(5, 1, 10)).isTrue();
        assertThat(ValidationUtils.isInRange(1, 1, 10)).isTrue();
        assertThat(ValidationUtils.isInRange(10, 1, 10)).isTrue();
        assertThat(ValidationUtils.isInRange(5.5, 1.0, 10.0)).isTrue();
        
        assertThat(ValidationUtils.isInRange(0, 1, 10)).isFalse();
        assertThat(ValidationUtils.isInRange(11, 1, 10)).isFalse();
        assertThat(ValidationUtils.isInRange(null, 1, 10)).isFalse();
    }

    @Test
    void shouldSanitizeInput() {
        // Given & When & Then
        assertThat(ValidationUtils.sanitizeInput("<script>")).isEqualTo("&lt;script&gt;");
        assertThat(ValidationUtils.sanitizeInput("\"test\"")).isEqualTo("&quot;test&quot;");
        assertThat(ValidationUtils.sanitizeInput("'test'")).isEqualTo("&#x27;test&#x27;");
        assertThat(ValidationUtils.sanitizeInput("A & B")).isEqualTo("A &amp; B");
        assertThat(ValidationUtils.sanitizeInput("  hello  ")).isEqualTo("hello");
        assertThat(ValidationUtils.sanitizeInput(null)).isNull();
    }

    @Test
    void shouldValidateNotBlankStrings() {
        // Given & When & Then
        assertThat(ValidationUtils.isNotBlank("hello")).isTrue();
        assertThat(ValidationUtils.isNotBlank("  hello  ")).isTrue();
        
        assertThat(ValidationUtils.isNotBlank("")).isFalse();
        assertThat(ValidationUtils.isNotBlank("   ")).isFalse();
        assertThat(ValidationUtils.isNotBlank(null)).isFalse();
    }

    @Test
    void shouldValidatePaginationParameters() {
        // Given & When & Then
        assertThat(ValidationUtils.isValidPaginationParams(0, 10)).isTrue();
        assertThat(ValidationUtils.isValidPaginationParams(1, 50)).isTrue();
        assertThat(ValidationUtils.isValidPaginationParams(null, null)).isTrue();
        assertThat(ValidationUtils.isValidPaginationParams(0, null)).isTrue();
        assertThat(ValidationUtils.isValidPaginationParams(null, 10)).isTrue();
        
        assertThat(ValidationUtils.isValidPaginationParams(-1, 10)).isFalse();
        assertThat(ValidationUtils.isValidPaginationParams(0, 0)).isFalse();
        assertThat(ValidationUtils.isValidPaginationParams(0, 101)).isFalse();
    }
}