package com.bookstore.loanmanagement.integration;

import com.bookstore.loanmanagement.dto.LoanRequest;
import com.bookstore.loanmanagement.dto.LoanResponse;
import com.bookstore.loanmanagement.dto.ReturnLoanRequest;
import com.bookstore.loanmanagement.entity.LoanStatus;
import com.bookstore.loanmanagement.repository.LoanRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LoanManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LoanRepository loanRepository;

    @BeforeEach
    void setUp() {
        loanRepository.deleteAll();
    }

    @Test
    void testCompleteLoanLifecycle() throws Exception {
        // Create a loan
        LoanRequest createRequest = new LoanRequest(1L, 1L, "Test loan");
        
        String createResponse = mockMvc.perform(post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.bookId").value(1))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        LoanResponse loan = objectMapper.readValue(createResponse, LoanResponse.class);
        Long loanId = loan.getId();
        
        // Get loan by ID
        mockMvc.perform(get("/api/loans/" + loanId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(loanId))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
        
        // Get loan history
        mockMvc.perform(get("/api/loans/" + loanId + "/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        
        // Extend loan
        mockMvc.perform(put("/api/loans/" + loanId + "/extend")
                .param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(loanId));
        
        // Return loan
        ReturnLoanRequest returnRequest = new ReturnLoanRequest("Book returned in good condition");
        
        mockMvc.perform(put("/api/loans/" + loanId + "/return")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(returnRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"))
                .andExpect(jsonPath("$.returnDate").exists());
    }

    @Test
    void testGetUserLoans() throws Exception {
        // Create multiple loans for the same user
        LoanRequest request1 = new LoanRequest(1L, 1L, "Loan 1");
        LoanRequest request2 = new LoanRequest(1L, 2L, "Loan 2");
        
        mockMvc.perform(post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());
        
        mockMvc.perform(post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());
        
        // Get all loans for user
        mockMvc.perform(get("/api/loans/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(2)));
        
        // Get active loans for user
        mockMvc.perform(get("/api/loans/user/1/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(2)));
    }

    @Test
    void testSearchLoans() throws Exception {
        // Create loans with different statuses
        LoanRequest request = new LoanRequest(1L, 1L, "Test loan");
        
        String response = mockMvc.perform(post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        LoanResponse loan = objectMapper.readValue(response, LoanResponse.class);
        
        // Search by user ID
        mockMvc.perform(get("/api/loans/search")
                .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)));
        
        // Search by book ID
        mockMvc.perform(get("/api/loans/search")
                .param("bookId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)));
        
        // Search by status
        mockMvc.perform(get("/api/loans/search")
                .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testLoanStatistics() throws Exception {
        // Create some loans
        LoanRequest request1 = new LoanRequest(1L, 1L, "Loan 1");
        LoanRequest request2 = new LoanRequest(2L, 2L, "Loan 2");
        
        mockMvc.perform(post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());
        
        mockMvc.perform(post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());
        
        // Get statistics
        mockMvc.perform(get("/api/loans/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeLoans").value(greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.totalLoans").value(greaterThanOrEqualTo(2)));
    }

    @Test
    void testLoanAnalytics() throws Exception {
        // Create a loan
        LoanRequest request = new LoanRequest(1L, 1L, "Test loan");
        
        mockMvc.perform(post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        
        // Get user loan count
        mockMvc.perform(get("/api/loans/analytics/user/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(greaterThanOrEqualTo("1").toString()));
        
        // Get book loan count
        mockMvc.perform(get("/api/loans/analytics/book/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(greaterThanOrEqualTo("1").toString()));
    }

    @Test
    void testInvalidLoanCreation() throws Exception {
        // Try to create loan with null values
        LoanRequest invalidRequest = new LoanRequest(null, null, null);
        
        mockMvc.perform(post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testReturnNonExistentLoan() throws Exception {
        ReturnLoanRequest returnRequest = new ReturnLoanRequest("Test");
        
        mockMvc.perform(put("/api/loans/99999/return")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(returnRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testExtendNonExistentLoan() throws Exception {
        mockMvc.perform(put("/api/loans/99999/extend")
                .param("days", "7"))
                .andExpect(status().isNotFound());
    }
}
