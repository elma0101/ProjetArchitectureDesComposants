package com.bookstore.config;

import com.bookstore.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RestExceptionHandlerTest {

    private RestExceptionHandler exceptionHandler;
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        exceptionHandler = new RestExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/books");
        webRequest = new ServletWebRequest(request);
    }

    @Test
    void shouldHandleBookNotFoundException() {
        // Given
        BookNotFoundException exception = new BookNotFoundException("Book not found with id: 1");

        // When
        ResponseEntity<Object> response = exceptionHandler.handleBookNotFoundException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("error")).isEqualTo("Book Not Found");
        assertThat(body.get("message")).isEqualTo("Book not found with id: 1");
        assertThat(body.get("status")).isEqualTo(404);
    }

    @Test
    void shouldHandleAuthorNotFoundException() {
        // Given
        AuthorNotFoundException exception = new AuthorNotFoundException("Author not found with id: 1");

        // When
        ResponseEntity<Object> response = exceptionHandler.handleAuthorNotFoundException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("error")).isEqualTo("Author Not Found");
        assertThat(body.get("message")).isEqualTo("Author not found with id: 1");
    }

    @Test
    void shouldHandleLoanNotFoundException() {
        // Given
        LoanNotFoundException exception = new LoanNotFoundException("Loan not found with id: 1");

        // When
        ResponseEntity<Object> response = exceptionHandler.handleLoanNotFoundException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("error")).isEqualTo("Loan Not Found");
        assertThat(body.get("message")).isEqualTo("Loan not found with id: 1");
    }

    @Test
    void shouldHandleBookNotAvailableException() {
        // Given
        BookNotAvailableException exception = new BookNotAvailableException("Book is not available for loan");

        // When
        ResponseEntity<Object> response = exceptionHandler.handleBookNotAvailableException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("error")).isEqualTo("Book Not Available");
        assertThat(body.get("message")).isEqualTo("Book is not available for loan");
    }

    @Test
    void shouldHandleValidationException() {
        // Given
        Map<String, String> fieldErrors = new HashMap<>();
        fieldErrors.put("title", "Title is required");
        fieldErrors.put("isbn", "Invalid ISBN format");
        
        ValidationException exception = new ValidationException("Validation failed", fieldErrors);

        // When
        ResponseEntity<Object> response = exceptionHandler.handleValidationException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("error")).isEqualTo("Validation Error");
        assertThat(body.get("message")).isEqualTo("Validation failed");
        assertThat(body.get("fieldErrors")).isEqualTo(fieldErrors);
    }

    @Test
    void shouldHandleDuplicateResourceException() {
        // Given
        DuplicateResourceException exception = new DuplicateResourceException("Book", "9780123456789");

        // When
        ResponseEntity<Object> response = exceptionHandler.handleDuplicateResourceException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("error")).isEqualTo("Duplicate Resource");
        assertThat(body.get("resourceType")).isEqualTo("Book");
        assertThat(body.get("identifier")).isEqualTo("9780123456789");
    }

    @Test
    void shouldHandleBusinessRuleViolationException() {
        // Given
        BusinessRuleViolationException exception = new BusinessRuleViolationException("BOOK_NOT_AVAILABLE", "Book is not available");

        // When
        ResponseEntity<Object> response = exceptionHandler.handleBusinessRuleViolationException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("error")).isEqualTo("Business Rule Violation");
        assertThat(body.get("message")).isEqualTo("Book is not available");
        assertThat(body.get("ruleCode")).isEqualTo("BOOK_NOT_AVAILABLE");
    }

    @Test
    void shouldHandleConstraintViolationException() {
        // Given
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));
        when(violation.getPropertyPath().toString()).thenReturn("title");
        when(violation.getInvalidValue()).thenReturn("");
        when(violation.getMessage()).thenReturn("Title is required");
        violations.add(violation);
        
        ConstraintViolationException exception = new ConstraintViolationException("Validation failed", violations);

        // When
        ResponseEntity<Object> response = exceptionHandler.handleConstraintViolationException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("error")).isEqualTo("Constraint Violation");
        assertThat(body.get("message")).isEqualTo("Validation constraints violated");
    }

    @Test
    void shouldHandleDataIntegrityViolationException() {
        // Given
        DataIntegrityViolationException exception = new DataIntegrityViolationException("unique constraint violation");

        // When
        ResponseEntity<Object> response = exceptionHandler.handleDataIntegrityViolationException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("error")).isEqualTo("Data Integrity Violation");
        assertThat(body.get("message")).isEqualTo("A record with this information already exists");
    }

    @Test
    void shouldHandleEntityNotFoundException() {
        // Given
        EntityNotFoundException exception = new EntityNotFoundException("Entity not found");

        // When
        ResponseEntity<Object> response = exceptionHandler.handleEntityNotFoundException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("error")).isEqualTo("Resource Not Found");
        assertThat(body.get("message")).isEqualTo("Entity not found");
    }

    @Test
    void shouldHandleIllegalArgumentException() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument provided");

        // When
        ResponseEntity<Object> response = exceptionHandler.handleIllegalArgumentException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("error")).isEqualTo("Invalid Argument");
        assertThat(body.get("message")).isEqualTo("Invalid argument provided");
    }

    @Test
    void shouldHandleGenericException() {
        // Given
        RuntimeException exception = new RuntimeException("Unexpected error");

        // When
        ResponseEntity<Object> response = exceptionHandler.handleGenericException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("error")).isEqualTo("Internal Server Error");
        assertThat(body.get("message")).isEqualTo("An unexpected error occurred");
        assertThat(body.get("status")).isEqualTo(500);
    }
}