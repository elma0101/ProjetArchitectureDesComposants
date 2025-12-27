package com.bookstore.loanmanagement.controller;

import com.bookstore.loanmanagement.dto.LoanRequest;
import com.bookstore.loanmanagement.dto.LoanResponse;
import com.bookstore.loanmanagement.dto.LoanStatistics;
import com.bookstore.loanmanagement.dto.ReturnLoanRequest;
import com.bookstore.loanmanagement.entity.LoanStatus;
import com.bookstore.loanmanagement.entity.LoanTracking;
import com.bookstore.loanmanagement.service.LoanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoanController.class)
class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LoanService loanService;

    private LoanResponse loanResponse;
    private LoanRequest loanRequest;

    @BeforeEach
    void setUp() {
        loanRequest = new LoanRequest(1L, 1L, "Test loan");
        
        loanResponse = new LoanResponse();
        loanResponse.setId(1L);
        loanResponse.setUserId(1L);
        loanResponse.setBookId(1L);
        loanResponse.setLoanDate(LocalDate.now());
        loanResponse.setDueDate(LocalDate.now().plusDays(14));
        loanResponse.setStatus(LoanStatus.ACTIVE);
        loanResponse.setCreatedAt(LocalDateTime.now());
        loanResponse.setUpdatedAt(LocalDateTime.now());
        loanResponse.setOverdue(false);
    }

    @Test
    void createLoan_ShouldReturnCreatedLoan() throws Exception {
        when(loanService.createLoan(any(LoanRequest.class))).thenReturn(loanResponse);

        mockMvc.perform(post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loanRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.bookId").value(1))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void createLoan_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        LoanRequest invalidRequest = new LoanRequest(null, null, null);

        mockMvc.perform(post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnLoan_ShouldReturnUpdatedLoan() throws Exception {
        loanResponse.setReturnDate(LocalDate.now());
        loanResponse.setStatus(LoanStatus.RETURNED);
        
        ReturnLoanRequest returnRequest = new ReturnLoanRequest("Returned in good condition");
        
        when(loanService.returnLoan(eq(1L), anyString())).thenReturn(loanResponse);

        mockMvc.perform(put("/api/loans/1/return")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(returnRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("RETURNED"))
                .andExpect(jsonPath("$.returnDate").exists());
    }

    @Test
    void returnLoan_WithoutNotes_ShouldSucceed() throws Exception {
        loanResponse.setReturnDate(LocalDate.now());
        loanResponse.setStatus(LoanStatus.RETURNED);
        
        when(loanService.returnLoan(eq(1L), isNull())).thenReturn(loanResponse);

        mockMvc.perform(put("/api/loans/1/return")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"));
    }

    @Test
    void extendLoan_ShouldReturnExtendedLoan() throws Exception {
        loanResponse.setDueDate(LocalDate.now().plusDays(21));
        
        when(loanService.extendLoan(eq(1L), eq(7))).thenReturn(loanResponse);

        mockMvc.perform(put("/api/loans/1/extend")
                .param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.dueDate").exists());
    }

    @Test
    void extendLoan_WithDefaultDays_ShouldUseDefault() throws Exception {
        loanResponse.setDueDate(LocalDate.now().plusDays(21));
        
        when(loanService.extendLoan(eq(1L), eq(7))).thenReturn(loanResponse);

        mockMvc.perform(put("/api/loans/1/extend"))
                .andExpect(status().isOk());
    }

    @Test
    void getLoanById_ShouldReturnLoan() throws Exception {
        when(loanService.getLoanById(1L)).thenReturn(loanResponse);

        mockMvc.perform(get("/api/loans/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.bookId").value(1));
    }

    @Test
    void getLoanHistory_ShouldReturnHistory() throws Exception {
        LoanTracking tracking1 = new LoanTracking();
        tracking1.setId(1L);
        tracking1.setLoanId(1L);
        tracking1.setStatus(LoanStatus.ACTIVE);
        tracking1.setTimestamp(LocalDateTime.now());
        
        LoanTracking tracking2 = new LoanTracking();
        tracking2.setId(2L);
        tracking2.setLoanId(1L);
        tracking2.setStatus(LoanStatus.RETURNED);
        tracking2.setTimestamp(LocalDateTime.now());
        
        List<LoanTracking> history = Arrays.asList(tracking1, tracking2);
        
        when(loanService.getLoanHistory(1L)).thenReturn(history);

        mockMvc.perform(get("/api/loans/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getUserLoans_ShouldReturnPaginatedLoans() throws Exception {
        Page<LoanResponse> page = new PageImpl<>(Arrays.asList(loanResponse), PageRequest.of(0, 20), 1);
        
        when(loanService.getLoansByUserId(eq(1L), any())).thenReturn(page);

        mockMvc.perform(get("/api/loans/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getActiveUserLoans_ShouldReturnActiveLoans() throws Exception {
        List<LoanResponse> loans = Arrays.asList(loanResponse);
        
        when(loanService.getActiveLoansForUser(1L)).thenReturn(loans);

        mockMvc.perform(get("/api/loans/user/1/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void getBookLoans_ShouldReturnLoansForBook() throws Exception {
        List<LoanResponse> loans = Arrays.asList(loanResponse);
        
        when(loanService.getLoansByBookId(1L)).thenReturn(loans);

        mockMvc.perform(get("/api/loans/book/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].bookId").value(1));
    }

    @Test
    void searchLoans_WithAllFilters_ShouldReturnFilteredLoans() throws Exception {
        List<LoanResponse> loans = Arrays.asList(loanResponse);
        
        when(loanService.searchLoans(any(), any(), any(), any(), any(), any())).thenReturn(loans);

        mockMvc.perform(get("/api/loans/search")
                .param("userId", "1")
                .param("bookId", "1")
                .param("status", "ACTIVE")
                .param("fromDate", "2024-01-01")
                .param("toDate", "2024-12-31")
                .param("overdue", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void searchLoans_WithNoFilters_ShouldReturnAllLoans() throws Exception {
        List<LoanResponse> loans = Arrays.asList(loanResponse);
        
        when(loanService.searchLoans(isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(loans);

        mockMvc.perform(get("/api/loans/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getOverdueLoans_ShouldReturnOverdueLoans() throws Exception {
        loanResponse.setOverdue(true);
        loanResponse.setStatus(LoanStatus.OVERDUE);
        List<LoanResponse> loans = Arrays.asList(loanResponse);
        
        when(loanService.getOverdueLoans()).thenReturn(loans);

        mockMvc.perform(get("/api/loans/overdue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].overdue").value(true));
    }

    @Test
    void updateOverdueLoans_ShouldReturnUpdatedCount() throws Exception {
        when(loanService.updateOverdueLoans()).thenReturn(5);

        mockMvc.perform(post("/api/loans/overdue/update"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    @Test
    void getLoanStatistics_ShouldReturnStatistics() throws Exception {
        LoanStatistics stats = new LoanStatistics(10L, 2L, 50L);
        
        when(loanService.getLoanStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/loans/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeLoans").value(10))
                .andExpect(jsonPath("$.overdueLoans").value(2))
                .andExpect(jsonPath("$.returnedLoans").value(50))
                .andExpect(jsonPath("$.totalLoans").value(62));
    }

    @Test
    void getUserLoanCount_ShouldReturnCount() throws Exception {
        when(loanService.getActiveLoanCountForUser(1L)).thenReturn(3L);

        mockMvc.perform(get("/api/loans/analytics/user/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }

    @Test
    void getBookLoanCount_ShouldReturnCount() throws Exception {
        when(loanService.getActiveLoanCountForBook(1L)).thenReturn(2L);

        mockMvc.perform(get("/api/loans/analytics/book/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));
    }
}
