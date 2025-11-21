package com.bookstore.controller;

import com.bookstore.config.TestSecurityConfig;
import com.bookstore.entity.Book;
import com.bookstore.entity.Loan;
import com.bookstore.entity.LoanStatus;
import com.bookstore.service.LoanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoanController.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestSecurityConfig.class)
class LoanControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private LoanService loanService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private Book testBook;
    private Loan testLoan;
    
    @BeforeEach
    void setUp() {
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setIsbn("978-0-123456-78-9");
        testBook.setTotalCopies(5);
        testBook.setAvailableCopies(3);
        
        testLoan = new Loan();
        testLoan.setId(1L);
        testLoan.setBook(testBook);
        testLoan.setBorrowerName("John Doe");
        testLoan.setBorrowerEmail("john.doe@example.com");
        testLoan.setBorrowerId("USER001");
        testLoan.setLoanDate(LocalDate.now());
        testLoan.setDueDate(LocalDate.now().plusDays(14));
        testLoan.setStatus(LoanStatus.ACTIVE);
    }
    
    @Test
    void borrowBook_Success() throws Exception {
        // Given
        LoanController.BorrowBookRequest request = new LoanController.BorrowBookRequest();
        request.setBookId(1L);
        request.setBorrowerName("John Doe");
        request.setBorrowerEmail("john.doe@example.com");
        request.setBorrowerId("USER001");
        request.setNotes("Test notes");
        
        when(loanService.borrowBook(1L, "John Doe", "john.doe@example.com", "USER001", "Test notes"))
            .thenReturn(testLoan);
        
        // When & Then
        mockMvc.perform(post("/api/loans/borrow")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.borrowerName").value("John Doe"))
                .andExpect(jsonPath("$.borrowerEmail").value("john.doe@example.com"))
                .andExpect(jsonPath("$.borrowerId").value("USER001"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
    
    @Test
    void borrowBook_InvalidRequest() throws Exception {
        // Given
        LoanController.BorrowBookRequest request = new LoanController.BorrowBookRequest();
        // Missing required fields
        
        // When & Then
        mockMvc.perform(post("/api/loans/borrow")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void returnBook_Success() throws Exception {
        // Given
        testLoan.setStatus(LoanStatus.RETURNED);
        testLoan.setReturnDate(LocalDate.now());
        
        LoanController.ReturnBookRequest request = new LoanController.ReturnBookRequest();
        request.setNotes("Returned in good condition");
        
        when(loanService.returnBook(1L, "Returned in good condition")).thenReturn(testLoan);
        
        // When & Then
        mockMvc.perform(put("/api/loans/1/return")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("RETURNED"));
    }
    
    @Test
    void returnBook_WithoutNotes() throws Exception {
        // Given
        testLoan.setStatus(LoanStatus.RETURNED);
        testLoan.setReturnDate(LocalDate.now());
        
        when(loanService.returnBook(1L, null)).thenReturn(testLoan);
        
        // When & Then
        mockMvc.perform(put("/api/loans/1/return")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("RETURNED"));
    }
    
    @Test
    void extendLoan_Success() throws Exception {
        // Given
        testLoan.setDueDate(LocalDate.now().plusDays(21));
        
        LoanController.ExtendLoanRequest request = new LoanController.ExtendLoanRequest();
        request.setAdditionalDays(7);
        
        when(loanService.extendLoan(1L, 7)).thenReturn(testLoan);
        
        // When & Then
        mockMvc.perform(put("/api/loans/1/extend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
    
    @Test
    void extendLoan_InvalidRequest() throws Exception {
        // Given
        LoanController.ExtendLoanRequest request = new LoanController.ExtendLoanRequest();
        // Missing additionalDays
        
        // When & Then
        mockMvc.perform(put("/api/loans/1/extend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void getActiveLoans_Success() throws Exception {
        // Given
        List<Loan> loans = Arrays.asList(testLoan);
        Page<Loan> loanPage = new PageImpl<>(loans, PageRequest.of(0, 20), 1);
        
        when(loanService.getActiveLoans(any(Pageable.class))).thenReturn(loanPage);
        
        // When & Then
        mockMvc.perform(get("/api/loans/active")
                .param("page", "0")
                .param("size", "20")
                .param("sortBy", "dueDate")
                .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
    
    @Test
    void getOverdueLoans_Success() throws Exception {
        // Given
        testLoan.setStatus(LoanStatus.OVERDUE);
        List<Loan> loans = Arrays.asList(testLoan);
        Page<Loan> loanPage = new PageImpl<>(loans, PageRequest.of(0, 20), 1);
        
        when(loanService.getOverdueLoans(any(Pageable.class))).thenReturn(loanPage);
        
        // When & Then
        mockMvc.perform(get("/api/loans/overdue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].status").value("OVERDUE"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
    
    @Test
    void getLoansByBorrowerEmail_Success() throws Exception {
        // Given
        List<Loan> loans = Arrays.asList(testLoan);
        Page<Loan> loanPage = new PageImpl<>(loans, PageRequest.of(0, 20), 1);
        
        when(loanService.getLoansByBorrowerEmail(eq("john.doe@example.com"), any(Pageable.class)))
            .thenReturn(loanPage);
        
        // When & Then
        mockMvc.perform(get("/api/loans/borrower/john.doe@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].borrowerEmail").value("john.doe@example.com"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
    
    @Test
    void getLoanHistory_Success() throws Exception {
        // Given
        List<Loan> loans = Arrays.asList(testLoan);
        Page<Loan> loanPage = new PageImpl<>(loans, PageRequest.of(0, 20), 1);
        
        when(loanService.getLoanHistoryByBorrower(eq("john.doe@example.com"), any(Pageable.class)))
            .thenReturn(loanPage);
        
        // When & Then
        mockMvc.perform(get("/api/loans/history/john.doe@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
    
    @Test
    void searchLoans_Success() throws Exception {
        // Given
        List<Loan> loans = Arrays.asList(testLoan);
        Page<Loan> loanPage = new PageImpl<>(loans, PageRequest.of(0, 20), 1);
        
        when(loanService.searchLoans(anyString(), anyString(), any(LoanStatus.class), 
                any(LocalDate.class), any(LocalDate.class), any(Pageable.class)))
            .thenReturn(loanPage);
        
        // When & Then
        mockMvc.perform(get("/api/loans/search")
                .param("borrowerEmail", "john.doe@example.com")
                .param("borrowerName", "John Doe")
                .param("status", "ACTIVE")
                .param("startDate", "2023-01-01")
                .param("endDate", "2023-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
    
    @Test
    void getLoansDueToday_Success() throws Exception {
        // Given
        List<Loan> loans = Arrays.asList(testLoan);
        when(loanService.getLoansDueToday()).thenReturn(loans);
        
        // When & Then
        mockMvc.perform(get("/api/loans/due-today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));
    }
    
    @Test
    void getLoansDueWithin_Success() throws Exception {
        // Given
        List<Loan> loans = Arrays.asList(testLoan);
        when(loanService.getLoansDueWithinDays(7)).thenReturn(loans);
        
        // When & Then
        mockMvc.perform(get("/api/loans/due-within")
                .param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));
    }
    
    @Test
    void getBookLoanStatus_Success() throws Exception {
        // Given
        when(loanService.isBookCurrentlyLoaned(1L)).thenReturn(true);
        
        // When & Then
        mockMvc.perform(get("/api/loans/book/1/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(1))
                .andExpect(jsonPath("$.isCurrentlyLoaned").value(true))
                .andExpect(jsonPath("$.status").value("LOANED"));
    }
    
    @Test
    void getBookLoanStatus_Available() throws Exception {
        // Given
        when(loanService.isBookCurrentlyLoaned(1L)).thenReturn(false);
        
        // When & Then
        mockMvc.perform(get("/api/loans/book/1/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(1))
                .andExpect(jsonPath("$.isCurrentlyLoaned").value(false))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }
    
    @Test
    void getLoanStatistics_Success() throws Exception {
        // Given
        LoanService.LoanStatistics statistics = new LoanService.LoanStatistics(5L, 2L, 10L);
        when(loanService.getLoanStatistics()).thenReturn(statistics);
        
        // When & Then
        mockMvc.perform(get("/api/loans/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeLoans").value(5))
                .andExpect(jsonPath("$.overdueLoans").value(2))
                .andExpect(jsonPath("$.returnedLoans").value(10))
                .andExpect(jsonPath("$.totalLoans").value(17));
    }
    
    @Test
    void getMostBorrowedBooks_Success() throws Exception {
        // Given
        Object[] bookData = {1L, "Test Book", 5L};
        List<Object[]> books = java.util.Collections.singletonList(bookData);
        Page<Object[]> bookPage = new PageImpl<>(books, PageRequest.of(0, 10), 1);
        
        when(loanService.getMostBorrowedBooks(any(Pageable.class))).thenReturn(bookPage);
        
        // When & Then
        mockMvc.perform(get("/api/loans/statistics/most-borrowed-books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }
    
    @Test
    void getMostActiveBorrowers_Success() throws Exception {
        // Given
        Object[] borrowerData = {"john.doe@example.com", "John Doe", 3L};
        List<Object[]> borrowers = java.util.Collections.singletonList(borrowerData);
        Page<Object[]> borrowerPage = new PageImpl<>(borrowers, PageRequest.of(0, 10), 1);
        
        when(loanService.getMostActiveBorrowers(any(Pageable.class))).thenReturn(borrowerPage);
        
        // When & Then
        mockMvc.perform(get("/api/loans/statistics/most-active-borrowers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }
    
    @Test
    void updateOverdueLoans_Success() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/loans/update-overdue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Overdue loans updated successfully"));
    }
    
    @Test
    void getLoanById_Success() throws Exception {
        // Given
        when(loanService.getLoanById(1L)).thenReturn(Optional.of(testLoan));
        
        // When & Then
        mockMvc.perform(get("/api/loans/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.borrowerName").value("John Doe"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
    
    @Test
    void getLoanById_NotFound() throws Exception {
        // Given
        when(loanService.getLoanById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        mockMvc.perform(get("/api/loans/1"))
                .andExpect(status().isNotFound());
    }
}