package com.bookstore.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class YearValidatorTest {

    private YearValidator validator;

    @Mock
    private ValidYear validYear;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new YearValidator();
        
        when(validYear.min()).thenReturn(1000);
        when(validYear.max()).thenReturn(2100);
        when(validYear.allowFuture()).thenReturn(false);
        
        validator.initialize(validYear);
    }

    @Test
    void shouldValidateValidYear() {
        // Given
        Integer year = 2020;

        // When
        boolean result = validator.isValid(year, context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldAcceptNullYear() {
        // Given
        Integer year = null;

        // When
        boolean result = validator.isValid(year, context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldRejectYearBelowMinimum() {
        // Given
        Integer year = 999;
        
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addConstraintViolation()).thenReturn(context);

        // When
        boolean result = validator.isValid(year, context);

        // Then
        assertThat(result).isFalse();
        verify(context).buildConstraintViolationWithTemplate("Year must be between 1000 and 2100");
    }

    @Test
    void shouldRejectYearAboveMaximum() {
        // Given
        Integer year = 2101;
        
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addConstraintViolation()).thenReturn(context);

        // When
        boolean result = validator.isValid(year, context);

        // Then
        assertThat(result).isFalse();
        verify(context).buildConstraintViolationWithTemplate("Year must be between 1000 and 2100");
    }

    @Test
    void shouldRejectFutureYearWhenNotAllowed() {
        // Given
        Integer year = LocalDate.now().getYear() + 2;
        
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addConstraintViolation()).thenReturn(context);

        // When
        boolean result = validator.isValid(year, context);

        // Then
        assertThat(result).isFalse();
        verify(context).buildConstraintViolationWithTemplate("Publication year cannot be more than one year in the future");
    }

    @Test
    void shouldAcceptNextYear() {
        // Given
        Integer year = LocalDate.now().getYear() + 1;

        // When
        boolean result = validator.isValid(year, context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldAcceptFutureYearWhenAllowed() {
        // Given
        Integer year = LocalDate.now().getYear() + 5;
        
        when(validYear.allowFuture()).thenReturn(true);
        validator.initialize(validYear);

        // When
        boolean result = validator.isValid(year, context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldValidateCurrentYear() {
        // Given
        Integer year = LocalDate.now().getYear();

        // When
        boolean result = validator.isValid(year, context);

        // Then
        assertThat(result).isTrue();
    }
}