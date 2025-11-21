package com.bookstore.rest;

import com.bookstore.dto.*;
import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import com.bookstore.repository.AuthorRepository;
import com.bookstore.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
class AdvancedBookRestIntegrationTest extends BaseRestIntegrationTest {
    

    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private AuthorRepository authorRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private Author testAuthor;
    private Book testBook;
    
    @BeforeEach
    void setUp() {
        // Clean up
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        
        // Create test author
        testAuthor = new Author();
        testAuthor.setFirstName("John");
        testAuthor.setLastName("Doe");
        testAuthor.setBiography("Test author biography");
        testAuthor = authorRepository.save(testAuthor);
        
        // Create test book
        testBook = new Book();
        testBook.setTitle("Test Book");
        testBook.setIsbn("978-0132350884");
        testBook.setDescription("Test Description");
        testBook.setGenre("Programming");
        testBook.setPublicationYear(2008);
        testBook.setAvailableCopies(3);
        testBook.setTotalCopies(5);
        testBook.addAuthor(testAuthor);
        testBook = bookRepository.save(testBook);
    }
    
    @Test
    void testBulkCreateBooks_Success() throws Exception {
        // Arrange
        BookCreateRequest bookRequest1 = new BookCreateRequest();
        bookRequest1.setTitle("Bulk Book 1");
        bookRequest1.setIsbn("978-1111111111");
        bookRequest1.setDescription("First bulk book");
        bookRequest1.setGenre("Fiction");
        bookRequest1.setAvailableCopies(2);
        bookRequest1.setTotalCopies(4);
        bookRequest1.setAuthorIds(Arrays.asList(testAuthor.getId()));
        
        BookCreateRequest bookRequest2 = new BookCreateRequest();
        bookRequest2.setTitle("Bulk Book 2");
        bookRequest2.setIsbn("978-2222222222");
        bookRequest2.setDescription("Second bulk book");
        bookRequest2.setGenre("Science");
        bookRequest2.setAvailableCopies(1);
        bookRequest2.setTotalCopies(3);
        
        BulkBookRequest bulkRequest = new BulkBookRequest();
        bulkRequest.setBooks(Arrays.asList(bookRequest1, bookRequest2));
        
        // Act & Assert
        mockMvc.perform(post("/api/books/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bulkRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successCount").value(2))
                .andExpect(jsonPath("$.failureCount").value(0))
                .andExpect(jsonPath("$.successfulItems").isArray())
                .andExpect(jsonPath("$.successfulItems", hasSize(2)))
                .andExpect(jsonPath("$.errors").isEmpty());
        
        // Verify books were created
        assert bookRepository.findByIsbn("978-1111111111").isPresent();
        assert bookRepository.findByIsbn("978-2222222222").isPresent();
    }
    
    @Test
    void testBulkCreateBooks_DuplicateIsbn() throws Exception {
        // Arrange
        BookCreateRequest bookRequest = new BookCreateRequest();
        bookRequest.setTitle("Duplicate Book");
        bookRequest.setIsbn(testBook.getIsbn()); // Use existing ISBN
        bookRequest.setDescription("This should fail");
        bookRequest.setGenre("Fiction");
        bookRequest.setAvailableCopies(1);
        bookRequest.setTotalCopies(2);
        
        BulkBookRequest bulkRequest = new BulkBookRequest();
        bulkRequest.setBooks(Arrays.asList(bookRequest));
        
        // Act & Assert
        mockMvc.perform(post("/api/books/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bulkRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.successCount").value(0))
                .andExpect(jsonPath("$.failureCount").value(1))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].message").value(containsString("already exists")));
    }
    
    @Test
    void testBulkUpdateBooks_Success() throws Exception {
        // Arrange
        BookUpdateRequest updateRequest = new BookUpdateRequest();
        updateRequest.setId(testBook.getId());
        updateRequest.setTitle("Updated Test Book");
        updateRequest.setAvailableCopies(4);
        updateRequest.setGenre("Updated Programming");
        
        // Act & Assert
        mockMvc.perform(put("/api/books/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Arrays.asList(updateRequest))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successCount").value(1))
                .andExpect(jsonPath("$.failureCount").value(0))
                .andExpect(jsonPath("$.successfulItems").isArray())
                .andExpect(jsonPath("$.successfulItems", hasSize(1)))
                .andExpect(jsonPath("$.successfulItems[0].title").value("Updated Test Book"))
                .andExpect(jsonPath("$.successfulItems[0].availableCopies").value(4));
        
        // Verify book was updated
        Book updatedBook = bookRepository.findById(testBook.getId()).orElseThrow();
        assert "Updated Test Book".equals(updatedBook.getTitle());
        assert updatedBook.getAvailableCopies() == 4;
    }
    
    @Test
    void testGetBookAvailabilityStatus() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/books/{id}/availability", testBook.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(testBook.getId()))
                .andExpect(jsonPath("$.title").value("Test Book"))
                .andExpect(jsonPath("$.availableCopies").value(3))
                .andExpect(jsonPath("$.totalCopies").value(5))
                .andExpect(jsonPath("$.isAvailable").value(true))
                .andExpect(jsonPath("$.availabilityPercentage").value(60.0))
                .andExpect(jsonPath("$.notifications").isArray())
                .andExpect(jsonPath("$.lastUpdated").exists());
    }
    
    @Test
    void testGetBookAvailabilityStatus_OutOfStock() throws Exception {
        // Arrange - make book out of stock
        testBook.setAvailableCopies(0);
        bookRepository.save(testBook);
        
        // Act & Assert
        mockMvc.perform(get("/api/books/{id}/availability", testBook.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAvailable").value(false))
                .andExpect(jsonPath("$.availabilityPercentage").value(0.0))
                .andExpect(jsonPath("$.notifications").isArray())
                .andExpect(jsonPath("$.notifications", hasSize(1)))
                .andExpect(jsonPath("$.notifications[0]").value(containsString("out of stock")));
    }
    
    @Test
    void testGetBooksWithLowStock() throws Exception {
        // Arrange - create a book with low stock
        Book lowStockBook = new Book();
        lowStockBook.setTitle("Low Stock Book");
        lowStockBook.setIsbn("978-9999999999");
        lowStockBook.setDescription("This book has low stock");
        lowStockBook.setGenre("Test");
        lowStockBook.setAvailableCopies(1);
        lowStockBook.setTotalCopies(5);
        bookRepository.save(lowStockBook);
        
        // Act & Assert
        mockMvc.perform(get("/api/books/low-stock")
                .param("threshold", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Low Stock Book"))
                .andExpect(jsonPath("$[0].availableCopies").value(1))
                .andExpect(jsonPath("$[0].notifications").isArray())
                .andExpect(jsonPath("$[0].notifications[0]").value(containsString("Low stock")));
    }
    
    @Test
    void testGetBookStatistics() throws Exception {
        // Arrange - create additional books for better statistics
        Book book2 = new Book();
        book2.setTitle("Statistics Book 2");
        book2.setIsbn("978-3333333333");
        book2.setGenre("Fiction");
        book2.setPublicationYear(2020);
        book2.setAvailableCopies(0);
        book2.setTotalCopies(3);
        bookRepository.save(book2);
        
        Book book3 = new Book();
        book3.setTitle("Statistics Book 3");
        book3.setIsbn("978-4444444444");
        book3.setGenre("Programming");
        book3.setPublicationYear(2021);
        book3.setAvailableCopies(2);
        book3.setTotalCopies(2);
        bookRepository.save(book3);
        
        // Act & Assert
        mockMvc.perform(get("/api/books/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBooks").value(3))
                .andExpect(jsonPath("$.totalCopies").value(10)) // 5 + 3 + 2
                .andExpect(jsonPath("$.availableCopies").value(5)) // 3 + 0 + 2
                .andExpect(jsonPath("$.outOfStockBooks").value(1))
                .andExpect(jsonPath("$.genreDistribution").exists())
                .andExpect(jsonPath("$.genreDistribution.programming").value(2))
                .andExpect(jsonPath("$.genreDistribution.fiction").value(1))
                .andExpect(jsonPath("$.publicationYearDistribution").exists())
                .andExpect(jsonPath("$.publicationYearDistribution.2008").value(1))
                .andExpect(jsonPath("$.publicationYearDistribution.2020").value(1))
                .andExpect(jsonPath("$.publicationYearDistribution.2021").value(1))
                .andExpect(jsonPath("$.availabilityRate").value(50.0))
                .andExpect(jsonPath("$.outOfStockRate").value(closeTo(33.33, 0.1)))
                .andExpect(jsonPath("$.generatedAt").exists());
    }
    
    @Test
    void testUploadBookImage_Success() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.jpg", "image/jpeg", "test image content".getBytes());
        
        // Act & Assert
        mockMvc.perform(multipart("/api/books/{id}/image", testBook.getId())
                .file(file))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/api/books/" + testBook.getId() + "/image/")))
                .andExpect(content().string(containsString("book_" + testBook.getId() + "_")))
                .andExpect(content().string(containsString(".jpg")));
        
        // Verify book was updated with image URL
        Book updatedBook = bookRepository.findById(testBook.getId()).orElseThrow();
        assert updatedBook.getImageUrl() != null;
        assert updatedBook.getImageUrl().contains("/api/books/" + testBook.getId() + "/image/");
    }
    
    @Test
    void testUploadBookImage_InvalidFileType() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.txt", "text/plain", "test content".getBytes());
        
        // Act & Assert
        mockMvc.perform(multipart("/api/books/{id}/image", testBook.getId())
                .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("File must be an image"));
    }
    
    @Test
    void testUploadBookImage_EmptyFile() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.jpg", "image/jpeg", new byte[0]);
        
        // Act & Assert
        mockMvc.perform(multipart("/api/books/{id}/image", testBook.getId())
                .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("File is empty"));
    }
    
    @Test
    void testDeleteBookImage_Success() throws Exception {
        // Arrange - first upload an image
        testBook.setImageUrl("/api/books/" + testBook.getId() + "/image/test.jpg");
        bookRepository.save(testBook);
        
        // Act & Assert
        mockMvc.perform(delete("/api/books/{id}/image", testBook.getId()))
                .andExpect(status().isNoContent());
        
        // Verify image URL was removed
        Book updatedBook = bookRepository.findById(testBook.getId()).orElseThrow();
        assert updatedBook.getImageUrl() == null;
    }
    
    @Test
    void testGetBookAvailabilityStatus_BookNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/books/999/availability"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void testUploadBookImage_BookNotFound() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.jpg", "image/jpeg", "test image content".getBytes());
        
        // Act & Assert
        mockMvc.perform(multipart("/api/books/999/image")
                .file(file))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void testDeleteBookImage_BookNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/books/999/image"))
                .andExpect(status().isNotFound());
    }
}