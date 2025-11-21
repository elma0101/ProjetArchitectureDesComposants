package com.bookstore.integration;

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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive integration tests for Loan REST endpoints
 */
@SpringBootTest
class LoanRestIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    private Book testBook;
    private Loan testLoan;

    @BeforeEach
    void setUpTestData() {
        // Clean up existing data
        loanRepository.deleteAll();
        bookRepository.deleteAll();
        authorRepository.deleteAll();

        // Create test author
        Author author = new Author();
        author.setFirstName("Test");
        author.setLastName("Author");
        author.setBiography("Test author");
        author.setNationality("American");
        author = authorRepository.save(author);

        // Create test book
        testBook = new Book();
        testBook.setTitle("Loanable Book");
        testBook.setIsbn("978-1111111111");
        testBook.setDescription("A book that can be loaned");
        testBook.setPublicationYear(2023);
        testBook.setGenre("Fiction");
        testBook.setAvailableCopies(3);
        testBook.setTotalCopies(5);
        testBook.setAuthors(Set.of(author));
        testBook = bookRepository.save(testBook);

        // Create test loan
        testLoan = new Loan();
        testLoan.setBook(testBook);
        testLoan.setBorrowerName("John Borrower");
        testLoan.setBorrowerEmail("john.borrower@example.com");
        testLoan.setBorrowerId("BORROWER001");
        testLoan.setLoanDate(LocalDate.now().minusDays(5));
        testLoan.setDueDate(LocalDate.now().plusDays(9)); // 14 days total
        testLoan.setStatus(LoanStatus.ACTIVE);
        testLoan.setNotes("Test loan");
        testLoan = loanRepository.save(testLoan);
    }

    @Test
    void shouldGetAllLoans() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.loans").isArray())
                .andExpect(jsonPath("$._embedded.loans[0].borrowerName").value("John Borrower"))
                .andExpect(jsonPath("$._embedded.loans[0].borrowerEmail").value("john.borrower@example.com"))
                .andExpect(jsonPath("$._embedded.loans[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    void shouldGetLoanById() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/loans/{id}", testLoan.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.borrowerName").value("John Borrower"))
                .andExpect(jsonPath("$.borrowerEmail").value("john.borrower@example.com"))
                .andExpect(jsonPath("$.borrowerId").value("BORROWER001"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.notes").value("Test loan"));
    }

    @Test
    void shouldCreateNewLoan() throws Exception {
        Map<String, Object> loanRequest = new HashMap<>();
        loanRequest.put("borrowerName", "Jane Borrower");
        loanRequest.put("borrowerEmail", "jane.borrower@example.com");
        loanRequest.put("borrowerId", "BORROWER002");
        loanRequest.put("loanDate", LocalDate.now().toString());
        loanRequest.put("dueDate", LocalDate.now().plusDays(14).toString());
        loanRequest.put("status", "ACTIVE");
        loanRequest.put("notes", "New test loan");
        loanRequest.put("book", "/api/books/" + testBook.getId());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(loanRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.borrowerName").value("Jane Borrower"))
                .andExpect(jsonPath("$.borrowerEmail").value("jane.borrower@example.com"))
                .andExpect(jsonPath("$.borrowerId").value("BORROWER002"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // Verify loan was saved to database
        List<Loan> loans = loanRepository.findAll();
        assertThat(loans).hasSize(2);
        assertThat(loans.stream().anyMatch(l -> l.getBorrowerName().equals("Jane Borrower"))).isTrue();
    }

    @Test
    void shouldUpdateExistingLoan() throws Exception {
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("borrowerName", "John Updated");
        updateRequest.put("notes", "Updated loan notes");
        updateRequest.put("status", "ACTIVE");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/loans/{id}", testLoan.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.borrowerName").value("John Updated"))
                .andExpect(jsonPath("$.notes").value("Updated loan notes"))
                .andExpect(jsonPath("$.borrowerEmail").value("john.borrower@example.com")); // Should remain unchanged

        // Verify loan was updated in database
        Loan updatedLoan = loanRepository.findById(testLoan.getId()).orElseThrow();
        assertThat(updatedLoan.getBorrowerName()).isEqualTo("John Updated");
        assertThat(updatedLoan.getNotes()).isEqualTo("Updated loan notes");
    }

    @Test
    void shouldReturnLoan() throws Exception {
        Map<String, Object> returnRequest = new HashMap<>();
        returnRequest.put("status", "RETURNED");
        returnRequest.put("returnDate", LocalDate.now().toString());

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/loans/{id}", testLoan.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(returnRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("RETURNED"))
                .andExpect(jsonPath("$.returnDate").value(LocalDate.now().toString()));

        // Verify loan status was updated in database
        Loan returnedLoan = loanRepository.findById(testLoan.getId()).orElseThrow();
        assertThat(returnedLoan.getStatus()).isEqualTo(LoanStatus.RETURNED);
        assertThat(returnedLoan.getReturnDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void shouldDeleteLoan() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/loans/{id}", testLoan.getId()))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Verify loan was deleted from database
        assertThat(loanRepository.findById(testLoan.getId())).isEmpty();
    }

    @Test
    void shouldReturnNotFoundForNonExistentLoan() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/loans/{id}", 99999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldSearchLoansByStatus() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/loans/search/findByStatus")
                        .param("status", "ACTIVE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.loans").isArray())
                .andExpect(jsonPath("$._embedded.loans[0].status").value("ACTIVE"));
    }

    @Test
    void shouldSearchLoansByBorrower() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/loans/search/findByBorrowerId")
                        .param("borrowerId", "BORROWER001")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.loans").isArray())
                .andExpect(jsonPath("$._embedded.loans[0].borrowerId").value("BORROWER001"));
    }

    @Test
    void shouldFindOverdueLoans() throws Exception {
        // Create an overdue loan
        Loan overdueLoan = new Loan();
        overdueLoan.setBook(testBook);
        overdueLoan.setBorrowerName("Overdue Borrower");
        overdueLoan.setBorrowerEmail("overdue@example.com");
        overdueLoan.setBorrowerId("OVERDUE001");
        overdueLoan.setLoanDate(LocalDate.now().minusDays(20));
        overdueLoan.setDueDate(LocalDate.now().minusDays(5)); // Past due date
        overdueLoan.setStatus(LoanStatus.OVERDUE);
        loanRepository.save(overdueLoan);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/loans/search/findByStatus")
                        .param("status", "OVERDUE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.loans").isArray())
                .andExpect(jsonPath("$._embedded.loans[0].status").value("OVERDUE"));
    }

    @Test
    void shouldGetLoanBook() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/loans/{id}/book", testLoan.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("Loanable Book"))
                .andExpect(jsonPath("$.isbn").value("978-1111111111"));
    }

    @Test
    void shouldGetLoansWithPagination() throws Exception {
        // Create additional loans for pagination test
        for (int i = 1; i <= 25; i++) {
            Loan loan = new Loan();
            loan.setBook(testBook);
            loan.setBorrowerName("Borrower " + i);
            loan.setBorrowerEmail("borrower" + i + "@example.com");
            loan.setBorrowerId("BORROWER" + String.format("%03d", i + 1));
            loan.setLoanDate(LocalDate.now().minusDays(i));
            loan.setDueDate(LocalDate.now().plusDays(14 - i));
            loan.setStatus(i % 3 == 0 ? LoanStatus.RETURNED : LoanStatus.ACTIVE);
            loanRepository.save(loan);
        }

        mockMvc.perform(MockMvcRequestBuilders.get("/api/loans")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "loanDate,desc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.loans").isArray())
                .andExpect(jsonPath("$._embedded.loans.length()").value(10))
                .andExpect(jsonPath("$.page.size").value(10))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(26)) // 25 + 1 original
                .andExpect(jsonPath("$.page.totalPages").value(3));
    }

    @Test
    void shouldHandleHATEOASLinks() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/loans/{id}", testLoan.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.loan.href").exists())
                .andExpect(jsonPath("$._links.book.href").exists());
    }

    @Test
    void shouldValidateLoanCreationRequest() throws Exception {
        Map<String, Object> invalidRequest = new HashMap<>();
        // Missing required fields

        mockMvc.perform(MockMvcRequestBuilders.post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleDateRangeQueries() throws Exception {
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/loans/search/findByLoanDateBetween")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.loans").isArray());
    }

    @Test
    void shouldHandleConcurrentLoanUpdates() throws Exception {
        Map<String, Object> request1 = new HashMap<>();
        request1.put("notes", "Concurrent update 1");
        request1.put("status", "ACTIVE");

        Map<String, Object> request2 = new HashMap<>();
        request2.put("notes", "Concurrent update 2");
        request2.put("status", "ACTIVE");

        // Both requests should succeed, but the last one should win
        mockMvc.perform(MockMvcRequestBuilders.put("/api/loans/{id}", testLoan.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request1)))
                .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.put("/api/loans/{id}", testLoan.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes").value("Concurrent update 2"));
    }
}