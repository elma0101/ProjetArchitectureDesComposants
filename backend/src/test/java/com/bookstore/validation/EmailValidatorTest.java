package com.bookstore.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class EmailValidatorTest {

    private EmailValidator validator;

    @Mock
    private ValidEmail validEmail;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new EmailValidator();
        
        when(validEmail.allowTestEmails()).thenReturn(false);
        when(validEmail.requireDomain()).thenReturn(true);
        
        validator.initialize(validEmail);
    }

    @Test
    void shouldValidateValidEmail() {
        // Given
        String email = "user@example.com";

        // When
        boolean result = validator.isValid(email, context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldRejectNullEmail() {
        // Given
        String email = null;

        // When
        boolean result = validator.isValid(email, context);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void shouldRejectEmptyEmail() {
        // Given
        String email = "";

        // When
        boolean result = validator.isValid(email, context);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void shouldRejectInvalidEmailFormat() {
        // Given
        String email = "invalid-email";

        // When
        boolean result = validator.isValid(email, context);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void shouldRejectTestEmailWhenNotAllowed() {
        // Given
        String email = "test@example.com";
        
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addConstraintViolation()).thenReturn(context);

        // When
        boolean result = validator.isValid(email, context);

        // Then
        assertThat(result).isFalse();
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("Test or example emails are not allowed");
    }

    @Test
    void shouldAcceptTestEmailWhenAllowed() {
        // Given
        String email = "test@example.com";
        
        when(validEmail.allowTestEmails()).thenReturn(true);
        validator.initialize(validEmail);

        // When
        boolean result = validator.isValid(email, context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldRejectDisposableEmail() {
        // Given
        String email = "user@10minutemail.com";
        
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addConstraintViolation()).thenReturn(context);

        // When
        boolean result = validator.isValid(email, context);

        // Then
        assertThat(result).isFalse();
        verify(context).buildConstraintViolationWithTemplate("Disposable email addresses are not allowed");
    }

    @Test
    void shouldValidateEmailWithValidDomain() {
        // Given
        String email = "user@company.org";

        // When
        boolean result = validator.isValid(email, context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldRejectEmailWithInvalidDomain() {
        // Given
        String email = "user@com";
        
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addConstraintViolation()).thenReturn(context);

        // When
        boolean result = validator.isValid(email, context);

        // Then
        assertThat(result).isFalse();
        verify(context).buildConstraintViolationWithTemplate("Email must have a valid domain");
    }

    @Test
    void shouldAcceptEmailWithoutDomainRequirement() {
        // Given
        String email = "user@com";
        
        when(validEmail.requireDomain()).thenReturn(false);
        validator.initialize(validEmail);

        // When
        boolean result = validator.isValid(email, context);

        // Then
        assertThat(result).isTrue();
    }
}