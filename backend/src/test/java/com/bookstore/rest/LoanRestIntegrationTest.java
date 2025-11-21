package com.bookstore.rest;

import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import com.bookstore.entity.Loan;
import com.bookstore.entity.LoanStatus;
import com.bookstore.repository.AuthorRepository;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class LoanRestIntegrationTest extends BaseRestIntegrationTest {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    private Loan testLoan;
    private Book testBook;
    private Author testAuthor;

    @BeforeEach
    void setUp() {
        loanRepository.deleteAll();
        bookRepository.deleteAll();
        authorRepository.deleteAll();

        // Create test author
        testAuthor = new Author("Test", "Author");
        testAuthor = authorRepository.save(testAuthor);

        // Create test book
        testBook = new Book("Test Book", "978-0-123456-78-9");
        testBook.setGenre("Fiction");
        testBook.setTotalCopies(5);
        testBook.setAvailableCopies(5);
        testBook.addAuthor(testAuthor);
        testBook = bookRepository.save(testBook);

        // Create test loan
        testLoan = new Loan(testBook, "John Borrower", "john@example.com", 
                           LocalDate.now(), LocalDate.now().plusDays(14));
        testLoan.setBorrowerId("BORROWER001");
        testLoan.setNotes("Test loan for integration testing");
        testLoan = loanRepository.save(testLoan);
    }

    @Test
    void shouldGetAllLoans() throws Exception {
        mockMvc.perform(get(LOANS_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.loans", hasSize(1)))
                .andExpect(jsonPath("$._embedded.loans[0].borrowerName", is("John Borrower")))
                .andExpect(jsonPath("$._embedded.loans[0].borrowerEmail", is("john@example.com")))
                .andExpect(jsonPath("$._embedded.loans[0].status", is("ACTIVE")))
                .andExpect(jsonPath("$.page.size", is(20)))
                .andExpect(jsonPath("$.page.totalElements", is(1)));
    }

    @Test
    void shouldGetLoanById() throws Exception {
        mockMvc.perform(get(LOANS_PATH + "/{id}", testLoan.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.borrowerName", is("John Borrower")))
                .andExpect(jsonPath("$.borrowerEmail", is("john@example.com")))
                .andExpect(jsonPath("$.borrowerId", is("BORROWER001")))
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.notes", is("Test loan for integration testing")));
    }

    @Test
    void shouldCreateNewLoan() throws Exception {
        // Create another book for the new loan
        Book anotherBook = new Book("Another Book", "978-0-987654-32-1");
        anotherBook.setGenre("Science Fiction");
        anotherBook.setTotalCopies(3);
        anotherBook.setAvailableCopies(3);
        anotherBook = bookRepository.save(anotherBook);

        Loan newLoan = new Loan();
        newLoan.setBook(anotherBook);
        newLoan.setBorrowerName("Jane Borrower");
        newLoan.setBorrowerEmail("jane@example.com");
        newLoan.setBorrowerId("BORROWER002");
        newLoan.setLoanDate(LocalDate.now());
        newLoan.setDueDate(LocalDate.now().plusDays(14));
        newLoan.setNotes("New test loan");

        mockMvc.perform(post(LOANS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newLoan)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.borrowerName", is("Jane Borrower")))
                .andExpect(jsonPath("$.borrowerEmail", is("jane@example.com")))
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    void shouldUpdateExistingLoan() throws Exception {
        testLoan.setNotes("Updated loan notes");
        testLoan.setStatus(LoanStatus.RETURNED);
        testLoan.setReturnDate(LocalDate.now());

        mockMvc.perform(put(LOANS_PATH + "/{id}", testLoan.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testLoan)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.notes", is("Updated loan notes")))
                .andExpect(jsonPath("$.status", is("RETURNED")))
                .andExpect(jsonPath("$.returnDate", is(LocalDate.now().toString())));
    }

    @Test
    void shouldPartiallyUpdateLoan() throws Exception {
        String partialUpdate = "{\"notes\":\"Partially updated notes\"}";

        mockMvc.perform(patch(LOANS_PATH + "/{id}", testLoan.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(partialUpdate))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.notes", is("Partially updated notes")))
                .andExpect(jsonPath("$.borrowerName", is("John Borrower")))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    void shouldSearchLoansByStatus() throws Exception {
        mockMvc.perform(get(LOANS_PATH + "/search/findByStatus")
                .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.loans", hasSize(1)))
                .andExpect(jsonPath("$._embedded.loans[0].status", is("ACTIVE")));
    }

    @Test
    void shouldSearchLoansByBorrowerEmail() throws Exception {
        mockMvc.perform(get(LOANS_PATH + "/search/findByBorrowerEmailIgnoreCase")
                .param("borrowerEmail", "john@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.loans", hasSize(1)))
                .andExpect(jsonPath("$._embedded.loans[0].borrowerEmail", is("john@example.com")));
    }

    @Test
    void shouldSearchLoansByBorrowerId() throws Exception {
        mockMvc.perform(get(LOANS_PATH + "/search/findByBorrowerId")
                .param("borrowerId", "BORROWER001"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.loans", hasSize(1)))
                .andExpect(jsonPath("$._embedded.loans[0].borrowerId", is("BORROWER001")));
    }

    @Test
    void shouldSearchLoansByBookId() throws Exception {
        mockMvc.perform(get(LOANS_PATH + "/search/findByBookId")
                .param("bookId", testBook.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.loans", hasSize(1)));
    }

    @Test
    void shouldFindOverdueLoans() throws Exception {
        // Create an overdue loan
        Loan overdueLoan = new Loan(testBook, "Overdue Borrower", "overdue@example.com",
                                   LocalDate.now().minusDays(20), LocalDate.now().minusDays(5));
        overdueLoan.setStatus(LoanStatus.ACTIVE);
        loanRepository.save(overdueLoan);

        mockMvc.perform(get(LOANS_PATH + "/search/findOverdueLoans"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.loans", hasSize(1)))
                .andExpect(jsonPath("$._embedded.loans[0].borrowerEmail", is("overdue@example.com")));
    }

    @Test
    void shouldHandlePagination() throws Exception {
        // Create additional loans for pagination test
        for (int i = 1; i <= 25; i++) {
            Book book = new Book("Book " + i, "978-0-123456-" + String.format("%02d", i) + "-0");
            book.setGenre("Test Genre");
            book.setTotalCopies(1);
            book.setAvailableCopies(1);
            book = bookRepository.save(book);

            Loan loan = new Loan(book, "Borrower " + i, "borrower" + i + "@example.com",
                               LocalDate.now(), LocalDate.now().plusDays(14));
            loanRepository.save(loan);
        }

        mockMvc.perform(get(LOANS_PATH)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.loans", hasSize(10)))
                .andExpect(jsonPath("$.page.size", is(10)))
                .andExpect(jsonPath("$.page.number", is(0)))
                .andExpect(jsonPath("$.page.totalPages", greaterThan(1)));
    }

    @Test
    void shouldHandleSorting() throws Exception {
        // Create additional loan with different borrower name for sorting
        Book anotherBook = new Book("Another Book", "978-0-111111-11-1");
        anotherBook.setGenre("Fiction");
        anotherBook.setTotalCopies(1);
        anotherBook.setAvailableCopies(1);
        anotherBook = bookRepository.save(anotherBook);

        Loan anotherLoan = new Loan(anotherBook, "Alice Borrower", "alice@example.com",
                                   LocalDate.now(), LocalDate.now().plusDays(14));
        loanRepository.save(anotherLoan);

        mockMvc.perform(get(LOANS_PATH)
                .param("sort", "borrowerName,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.loans[0].borrowerName", is("Alice Borrower")))
                .andExpect(jsonPath("$._embedded.loans[1].borrowerName", is("John Borrower")));
    }

    @Test
    void shouldReturnNotFoundForNonExistentLoan() throws Exception {
        mockMvc.perform(get(LOANS_PATH + "/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldValidateRequiredFields() throws Exception {
        Loan invalidLoan = new Loan();
        // Missing required fields: book, borrowerName, borrowerEmail, loanDate, dueDate

        mockMvc.perform(post(LOANS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidLoan)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors", hasSize(greaterThan(0))));
    }

    @Test
    void shouldValidateEmailFormat() throws Exception {
        Loan invalidLoan = new Loan();
        invalidLoan.setBook(testBook);
        invalidLoan.setBorrowerName("Test Borrower");
        invalidLoan.setBorrowerEmail("invalid-email"); // Invalid email format
        invalidLoan.setLoanDate(LocalDate.now());
        invalidLoan.setDueDate(LocalDate.now().plusDays(14));

        mockMvc.perform(post(LOANS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidLoan)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'borrowerEmail')].message", 
                    hasItem(containsString("email"))));
    }

    @Test
    void shouldPreventLoanWhenBookUnavailable() throws Exception {
        // Make the book unavailable
        testBook.setAvailableCopies(0);
        bookRepository.save(testBook);

        Loan newLoan = new Loan();
        newLoan.setBook(testBook);
        newLoan.setBorrowerName("Another Borrower");
        newLoan.setBorrowerEmail("another@example.com");
        newLoan.setLoanDate(LocalDate.now());
        newLoan.setDueDate(LocalDate.now().plusDays(14));

        mockMvc.perform(post(LOANS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newLoan)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("not available")));
    }

    @Test
    void shouldIncludeHATEOASLinks() throws Exception {
        mockMvc.perform(get(LOANS_PATH + "/{id}", testLoan.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href", notNullValue()))
                .andExpect(jsonPath("$._links.loan.href", notNullValue()))
                .andExpect(jsonPath("$._links.book.href", notNullValue()));
    }
}