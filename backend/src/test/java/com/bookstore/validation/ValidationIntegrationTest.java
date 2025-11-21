package com.bookstore.validation;

import com.bookstore.entity.Book;
import com.bookstore.entity.Loan;
import com.bookstore.entity.LoanStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class ValidationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldValidateBookCreationWithInvalidData() throws Exception {
        // Given
        Book book = new Book();
        book.setTitle(""); // Invalid: blank title
        book.setIsbn("invalid-isbn"); // Invalid: bad ISBN format
        book.setPublicationYear(3000); // Invalid: future year
        book.setAvailableCopies(10);
        book.setTotalCopies(5); // Invalid: available > total

        // When & Then
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldValidateLoanCreationWithInvalidData() throws Exception {
        // Given
        Loan loan = new Loan();
        loan.setBorrowerName(""); // Invalid: blank name
        loan.setBorrowerEmail("invalid-email"); // Invalid: bad email format
        loan.setLoanDate(LocalDate.now().plusDays(1)); // Invalid: future loan date
        loan.setDueDate(LocalDate.now().minusDays(1)); // Invalid: due date before loan date
        loan.setStatus(LoanStatus.ACTIVE);

        // When & Then
        mockMvc.perform(post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loan)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRejectSqlInjectionInParameters() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/books")
                .param("title", "'; DROP TABLE books; --"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid request parameters"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRejectXssInParameters() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/books")
                .param("title", "<script>alert('xss')</script>"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid request parameters"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldValidateSearchParameters() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/books/search")
                .param("query", "a") // Too short
                .param("page", "-1") // Negative page
                .param("size", "101")) // Too large size
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAcceptValidBookData() throws Exception {
        // Given
        Book book = new Book();
        book.setTitle("Clean Code");
        book.setIsbn("9780132350884");
        book.setDescription("A handbook of agile software craftsmanship");
        book.setPublicationYear(2008);
        book.setGenre("Programming");
        book.setAvailableCopies(5);
        book.setTotalCopies(10);

        // When & Then
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAcceptValidSearchParameters() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/books/search")
                .param("query", "programming")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowHealthCheckWithoutAuthentication() throws Exception {
        // When & Then
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowActuatorEndpointsWithoutValidation() throws Exception {
        // When & Then
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }
}