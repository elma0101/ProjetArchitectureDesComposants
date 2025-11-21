package com.bookstore.controller;

import com.bookstore.dto.*;
import com.bookstore.entity.Book;
import com.bookstore.exception.BookNotFoundException;
import com.bookstore.service.AdvancedBookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdvancedBookController.class)
@Import(com.bookstore.config.TestSecurityConfig.class)
class AdvancedBookControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private AdvancedBookService advancedBookService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private Book testBook;
    private BookCreateRequest bookCreateRequest;
    private BulkBookRequest bulkBookRequest;
    
    @BeforeEach
    void setUp() {
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setIsbn("978-0132350884");
        testBook.setDescription("Test Description");
        testBook.setGenre("Programming");
        testBook.setAvailableCopies(3);
        testBook.setTotalCopies(5);
        
        bookCreateRequest = new BookCreateRequest();
        bookCreateRequest.setTitle("New Book");
        bookCreateRequest.setIsbn("978-1234567890");
        bookCreateRequest.setDescription("New Description");
        bookCreateRequest.setGenre("Fiction");
        bookCreateRequest.setAvailableCopies(2);
        bookCreateRequest.setTotalCopies(4);
        
        bulkBookRequest = new BulkBookRequest();
        bulkBookRequest.setBooks(Arrays.asList(bookCreateRequest));
    }
    
    @Test
    void testBulkCreateBooks_Success() throws Exception {
        // Arrange
        BulkOperationResult<Book> result = new BulkOperationResult<>();
        result.addSuccessfulItem(testBook);
        
        when(advancedBookService.bulkCreateBooks(any(BulkBookRequest.class))).thenReturn(result);
        
        // Act & Assert
        mockMvc.perform(post("/api/books/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bulkBookRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successCount").value(1))
                .andExpect(jsonPath("$.failureCount").value(0))
                .andExpect(jsonPath("$.successfulItems").isArray())
                .andExpect(jsonPath("$.successfulItems[0].id").value(1))
                .andExpect(jsonPath("$.errors").isEmpty());
        
        verify(advancedBookService).bulkCreateBooks(any(BulkBookRequest.class));
    }
    
    @Test
    void testBulkCreateBooks_PartialFailure() throws Exception {
        // Arrange
        BulkOperationResult<Book> result = new BulkOperationResult<>();
        result.addSuccessfulItem(testBook);
        result.addError(new BulkOperationResult.BulkOperationError(1, "Duplicate ISBN", bookCreateRequest));
        
        when(advancedBookService.bulkCreateBooks(any(BulkBookRequest.class))).thenReturn(result);
        
        // Act & Assert
        mockMvc.perform(post("/api/books/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bulkBookRequest)))
                .andExpect(status().isMultiStatus())
                .andExpect(jsonPath("$.successCount").value(1))
                .andExpect(jsonPath("$.failureCount").value(1))
                .andExpect(jsonPath("$.successfulItems").isArray())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].message").value("Duplicate ISBN"));
        
        verify(advancedBookService).bulkCreateBooks(any(BulkBookRequest.class));
    }
    
    @Test
    void testBulkCreateBooks_InvalidRequest() throws Exception {
        // Arrange
        BulkBookRequest invalidRequest = new BulkBookRequest();
        invalidRequest.setBooks(Collections.emptyList());
        
        // Act & Assert
        mockMvc.perform(post("/api/books/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        
        verify(advancedBookService, never()).bulkCreateBooks(any(BulkBookRequest.class));
    }
    
    @Test
    void testBulkUpdateBooks_Success() throws Exception {
        // Arrange
        BookUpdateRequest updateRequest = new BookUpdateRequest();
        updateRequest.setId(1L);
        updateRequest.setTitle("Updated Title");
        
        BulkOperationResult<Book> result = new BulkOperationResult<>();
        result.addSuccessfulItem(testBook);
        
        when(advancedBookService.bulkUpdateBooks(anyList())).thenReturn(result);
        
        // Act & Assert
        mockMvc.perform(put("/api/books/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Arrays.asList(updateRequest))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successCount").value(1))
                .andExpect(jsonPath("$.failureCount").value(0));
        
        verify(advancedBookService).bulkUpdateBooks(anyList());
    }
    
    @Test
    void testGetBookAvailabilityStatus_Success() throws Exception {
        // Arrange
        BookAvailabilityStatus status = new BookAvailabilityStatus();
        status.setBookId(1L);
        status.setTitle("Test Book");
        status.setAvailableCopies(3);
        status.setTotalCopies(5);
        status.setIsAvailable(true);
        status.setAvailabilityPercentage(60.0);
        status.setNotifications(Collections.emptyList());
        status.setLastUpdated(LocalDateTime.now());
        
        when(advancedBookService.getBookAvailabilityStatus(1L)).thenReturn(status);
        
        // Act & Assert
        mockMvc.perform(get("/api/books/1/availability"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(1))
                .andExpect(jsonPath("$.title").value("Test Book"))
                .andExpect(jsonPath("$.availableCopies").value(3))
                .andExpect(jsonPath("$.totalCopies").value(5))
                .andExpect(jsonPath("$.isAvailable").value(true))
                .andExpect(jsonPath("$.availabilityPercentage").value(60.0));
        
        verify(advancedBookService).getBookAvailabilityStatus(1L);
    }
    
    @Test
    void testGetBookAvailabilityStatus_BookNotFound() throws Exception {
        // Arrange
        when(advancedBookService.getBookAvailabilityStatus(999L))
            .thenThrow(new BookNotFoundException("Book not found"));
        
        // Act & Assert
        mockMvc.perform(get("/api/books/999/availability"))
                .andExpect(status().isNotFound());
        
        verify(advancedBookService).getBookAvailabilityStatus(999L);
    }
    
    @Test
    void testGetBooksWithLowStock() throws Exception {
        // Arrange
        BookAvailabilityStatus status = new BookAvailabilityStatus();
        status.setBookId(1L);
        status.setTitle("Test Book");
        status.setAvailableCopies(1);
        status.setTotalCopies(5);
        status.setNotifications(Arrays.asList("Low stock: 1 copies remaining"));
        
        when(advancedBookService.getBooksWithLowStock(2)).thenReturn(Arrays.asList(status));
        
        // Act & Assert
        mockMvc.perform(get("/api/books/low-stock")
                .param("threshold", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].bookId").value(1))
                .andExpect(jsonPath("$[0].availableCopies").value(1))
                .andExpect(jsonPath("$[0].notifications[0]").value("Low stock: 1 copies remaining"));
        
        verify(advancedBookService).getBooksWithLowStock(2);
    }
    
    @Test
    void testGetBookStatistics() throws Exception {
        // Arrange
        BookStatistics statistics = new BookStatistics();
        statistics.setTotalBooks(10L);
        statistics.setTotalCopies(50);
        statistics.setAvailableCopies(30);
        statistics.setOutOfStockBooks(2);
        statistics.setGenreDistribution(Map.of("Programming", 5L, "Fiction", 3L));
        statistics.setPublicationYearDistribution(Map.of(2020, 3L, 2021, 4L));
        statistics.setGeneratedAt(LocalDateTime.now());
        
        when(advancedBookService.getBookStatistics()).thenReturn(statistics);
        
        // Act & Assert
        mockMvc.perform(get("/api/books/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBooks").value(10))
                .andExpect(jsonPath("$.totalCopies").value(50))
                .andExpect(jsonPath("$.availableCopies").value(30))
                .andExpect(jsonPath("$.outOfStockBooks").value(2))
                .andExpect(jsonPath("$.genreDistribution.Programming").value(5))
                .andExpect(jsonPath("$.publicationYearDistribution.2020").value(3))
                .andExpect(jsonPath("$.availabilityRate").value(60.0))
                .andExpect(jsonPath("$.outOfStockRate").value(20.0));
        
        verify(advancedBookService).getBookStatistics();
    }
    
    @Test
    void testUploadBookImage_Success() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.jpg", "image/jpeg", "test image content".getBytes());
        
        String expectedUrl = "/api/books/1/image/book_1_123456789.jpg";
        when(advancedBookService.uploadBookImage(eq(1L), any())).thenReturn(expectedUrl);
        
        // Act & Assert
        mockMvc.perform(multipart("/api/books/1/image")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedUrl));
        
        verify(advancedBookService).uploadBookImage(eq(1L), any());
    }
    
    @Test
    void testUploadBookImage_BookNotFound() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.jpg", "image/jpeg", "test image content".getBytes());
        
        when(advancedBookService.uploadBookImage(eq(999L), any()))
            .thenThrow(new BookNotFoundException("Book not found"));
        
        // Act & Assert
        mockMvc.perform(multipart("/api/books/999/image")
                .file(file))
                .andExpect(status().isNotFound());
        
        verify(advancedBookService).uploadBookImage(eq(999L), any());
    }
    
    @Test
    void testUploadBookImage_InvalidFile() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.txt", "text/plain", "test content".getBytes());
        
        when(advancedBookService.uploadBookImage(eq(1L), any()))
            .thenThrow(new IllegalArgumentException("File must be an image"));
        
        // Act & Assert
        mockMvc.perform(multipart("/api/books/1/image")
                .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("File must be an image"));
        
        verify(advancedBookService).uploadBookImage(eq(1L), any());
    }
    
    @Test
    void testDeleteBookImage_Success() throws Exception {
        // Arrange
        doNothing().when(advancedBookService).deleteBookImage(1L);
        
        // Act & Assert
        mockMvc.perform(delete("/api/books/1/image"))
                .andExpect(status().isNoContent());
        
        verify(advancedBookService).deleteBookImage(1L);
    }
    
    @Test
    void testDeleteBookImage_BookNotFound() throws Exception {
        // Arrange
        doThrow(new BookNotFoundException("Book not found"))
            .when(advancedBookService).deleteBookImage(999L);
        
        // Act & Assert
        mockMvc.perform(delete("/api/books/999/image"))
                .andExpect(status().isNotFound());
        
        verify(advancedBookService).deleteBookImage(999L);
    }
}