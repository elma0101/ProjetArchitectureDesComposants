package com.bookstore.validation;

import com.bookstore.entity.Loan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class DateRangeValidatorTest {

    private DateRangeValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DateRangeValidator();
        ValidDateRange annotation = new ValidDateRange() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return ValidDateRange.class;
            }

            @Override
            public String message() {
                return "Loan date must be before due date";
            }

            @Override
            public Class<?>[] groups() {
                return new Class[0];
            }

            @Override
            public Class<? extends jakarta.validation.Payload>[] payload() {
                return new Class[0];
            }

            @Override
            public String startDateField() {
                return "loanDate";
            }

            @Override
            public String endDateField() {
                return "dueDate";
            }
        };
        validator.initialize(annotation);
    }

    @Test
    void shouldValidateValidDateRange() {
        Loan loan = new Loan();
        loan.setLoanDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(14));

        assertThat(validator.isValid(loan, null)).isTrue();
    }

    @Test
    void shouldRejectInvalidDateRange() {
        Loan loan = new Loan();
        loan.setLoanDate(LocalDate.now().plusDays(14));
        loan.setDueDate(LocalDate.now());

        assertThat(validator.isValid(loan, null)).isFalse();
    }

    @Test
    void shouldValidateSameDates() {
        Loan loan = new Loan();
        LocalDate today = LocalDate.now();
        loan.setLoanDate(today);
        loan.setDueDate(today);

        assertThat(validator.isValid(loan, null)).isTrue();
    }

    @Test
    void shouldValidateNullDates() {
        Loan loan = new Loan();
        loan.setLoanDate(null);
        loan.setDueDate(null);

        assertThat(validator.isValid(loan, null)).isTrue();
    }

    @Test
    void shouldValidatePartialNullDates() {
        Loan loan1 = new Loan();
        loan1.setLoanDate(LocalDate.now());
        loan1.setDueDate(null);

        Loan loan2 = new Loan();
        loan2.setLoanDate(null);
        loan2.setDueDate(LocalDate.now());

        assertThat(validator.isValid(loan1, null)).isTrue();
        assertThat(validator.isValid(loan2, null)).isTrue();
    }

    @Test
    void shouldValidateNullObject() {
        assertThat(validator.isValid(null, null)).isTrue();
    }
}