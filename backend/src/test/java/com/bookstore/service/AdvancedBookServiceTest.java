package com.bookstore.service;

import com.bookstore.dto.*;
import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import com.bookstore.exception.BookNotFoundException;
import com.bookstore.repository.AuthorRepository;
import com.bookstore.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvancedBookServiceTest {
    
    @Mock
    private BookRepository bookRepository;
    
    @Mock
    private AuthorRepository authorRepository;
    
    @InjectMocks
    private AdvancedBookService advancedBookService;
    
    private Book testBook;
    private Author testAuthor;
    private BookCreateRequest bookCreateRequest;
    
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
        
        testAuthor = new Author();
        testAuthor.setId(1L);
        testAuthor.setFirstName("John");
        testAuthor.setLastName("Doe");
        
        bookCreateRequest = new BookCreateRequest();
        bookCreateRequest.setTitle("New Book");
        bookCreateRequest.setIsbn("978-1234567890");
        bookCreateRequest.setDescription("New Description");
        bookCreateRequest.setGenre("Fiction");
        bookCreateRequest.setAvailableCopies(2);
        bookCreateRequest.setTotalCopies(4);
        bookCreateRequest.setAuthorIds(Arrays.asList(1L));
    }
    
    @Test
    void testBulkCreateBooks_Success() {
        // Arrange
        BulkBookRequest request = new BulkBookRequest();
        request.setBooks(Arrays.asList(bookCreateRequest));
        
        when(bookRepository.existsByIsbn(anyString())).thenReturn(false);
        when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);
        
        // Act
        BulkOperationResult<Book> result = advancedBookService.bulkCreateBooks(request);
        
        // Assert
        assertEquals(1, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        assertEquals(1, result.getSuccessfulItems().size());
        assertTrue(result.getErrors().isEmpty());
        
        verify(bookRepository).existsByIsbn("978-1234567890");
        verify(bookRepository).save(any(Book.class));
    }
    
    @Test
    void testBulkCreateBooks_DuplicateIsbn() {
        // Arrange
        BulkBookRequest request = new BulkBookRequest();
        request.setBooks(Arrays.asList(bookCreateRequest));
        
        when(bookRepository.existsByIsbn(anyString())).thenReturn(true);
        
        // Act
        BulkOperationResult<Book> result = advancedBookService.bulkCreateBooks(request);
        
        // Assert
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
        assertTrue(result.getSuccessfulItems().isEmpty());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).getMessage().contains("already exists"));
        
        verify(bookRepository, never()).save(any(Book.class));
    }
    
    @Test
    void testBulkUpdateBooks_Success() {
        // Arrange
        BookUpdateRequest updateRequest = new BookUpdateRequest();
        updateRequest.setId(1L);
        updateRequest.setTitle("Updated Title");
        updateRequest.setAvailableCopies(4);
        
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);
        
        // Act
        BulkOperationResult<Book> result = advancedBookService.bulkUpdateBooks(Arrays.asList(updateRequest));
        
        // Assert
        assertEquals(1, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        assertEquals(1, result.getSuccessfulItems().size());
        assertTrue(result.getErrors().isEmpty());
        
        verify(bookRepository).findById(1L);
        verify(bookRepository).save(any(Book.class));
    }
    
    @Test
    void testBulkUpdateBooks_BookNotFound() {
        // Arrange
        BookUpdateRequest updateRequest = new BookUpdateRequest();
        updateRequest.setId(999L);
        updateRequest.setTitle("Updated Title");
        
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act
        BulkOperationResult<Book> result = advancedBookService.bulkUpdateBooks(Arrays.asList(updateRequest));
        
        // Assert
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
        assertTrue(result.getSuccessfulItems().isEmpty());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).getMessage().contains("not found"));
        
        verify(bookRepository, never()).save(any(Book.class));
    }
    
    @Test
    void testGetBookAvailabilityStatus_Success() {
        // Arrange
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        
        // Act
        BookAvailabilityStatus status = advancedBookService.getBookAvailabilityStatus(1L);
        
        // Assert
        assertNotNull(status);
        assertEquals(1L, status.getBookId());
        assertEquals("Test Book", status.getTitle());
        assertEquals(3, status.getAvailableCopies());
        assertEquals(5, status.getTotalCopies());
        assertTrue(status.getIsAvailable());
        assertEquals(60.0, status.getAvailabilityPercentage());
        assertNotNull(status.getLastUpdated());
        
        verify(bookRepository).findById(1L);
    }
    
    @Test
    void testGetBookAvailabilityStatus_BookNotFound() {
        // Arrange
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(BookNotFoundException.class, () -> {
            advancedBookService.getBookAvailabilityStatus(999L);
        });
        
        verify(bookRepository).findById(999L);
    }
    
    @Test
    void testGetBookAvailabilityStatus_OutOfStock() {
        // Arrange
        testBook.setAvailableCopies(0);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        
        // Act
        BookAvailabilityStatus status = advancedBookService.getBookAvailabilityStatus(1L);
        
        // Assert
        assertNotNull(status);
        assertFalse(status.getIsAvailable());
        assertEquals(0.0, status.getAvailabilityPercentage());
        assertFalse(status.getNotifications().isEmpty());
        assertTrue(status.getNotifications().get(0).contains("out of stock"));
    }
    
    @Test
    void testGetBookAvailabilityStatus_LowStock() {
        // Arrange
        testBook.setAvailableCopies(1);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        
        // Act
        BookAvailabilityStatus status = advancedBookService.getBookAvailabilityStatus(1L);
        
        // Assert
        assertNotNull(status);
        assertTrue(status.getIsAvailable());
        assertFalse(status.getNotifications().isEmpty());
        assertTrue(status.getNotifications().get(0).contains("Low stock warning"));
    }
    
    @Test
    void testGetBooksWithLowStock() {
        // Arrange
        List<Book> lowStockBooks = Arrays.asList(testBook);
        when(bookRepository.findBooksWithLowStock(2)).thenReturn(lowStockBooks);
        
        // Act
        List<BookAvailabilityStatus> result = advancedBookService.getBooksWithLowStock(2);
        
        // Assert
        assertEquals(1, result.size());
        BookAvailabilityStatus status = result.get(0);
        assertEquals(1L, status.getBookId());
        assertEquals("Test Book", status.getTitle());
        assertFalse(status.getNotifications().isEmpty());
        assertTrue(status.getNotifications().get(0).contains("Low stock"));
        
        verify(bookRepository).findBooksWithLowStock(2);
    }
    
    @Test
    void testGetBookStatistics() {
        // Arrange
        List<Book> allBooks = Arrays.asList(testBook);
        when(bookRepository.count()).thenReturn(1L);
        when(bookRepository.findAll()).thenReturn(allBooks);
        when(bookRepository.findOutOfStockBooks()).thenReturn(Collections.emptyList());
        
        // Act
        BookStatistics statistics = advancedBookService.getBookStatistics();
        
        // Assert
        assertNotNull(statistics);
        assertEquals(1L, statistics.getTotalBooks());
        assertEquals(5, statistics.getTotalCopies());
        assertEquals(3, statistics.getAvailableCopies());
        assertEquals(0, statistics.getOutOfStockBooks());
        assertNotNull(statistics.getGenreDistribution());
        assertNotNull(statistics.getPublicationYearDistribution());
        assertNotNull(statistics.getGeneratedAt());
        assertEquals(60.0, statistics.getAvailabilityRate());
        assertEquals(0.0, statistics.getOutOfStockRate());
        
        verify(bookRepository).count();
        verify(bookRepository).findAll();
        verify(bookRepository).findOutOfStockBooks();
    }
    
    @Test
    void testUploadBookImage_Success() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.jpg", "image/jpeg", "test image content".getBytes());
        
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);
        
        // Act
        String imageUrl = advancedBookService.uploadBookImage(1L, file);
        
        // Assert
        assertNotNull(imageUrl);
        assertTrue(imageUrl.contains("/api/books/1/image/"));
        assertTrue(imageUrl.contains("book_1_"));
        assertTrue(imageUrl.endsWith(".jpg"));
        
        verify(bookRepository).findById(1L);
        verify(bookRepository).save(any(Book.class));
    }
    
    @Test
    void testUploadBookImage_BookNotFound() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.jpg", "image/jpeg", "test image content".getBytes());
        
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(BookNotFoundException.class, () -> {
            advancedBookService.uploadBookImage(999L, file);
        });
        
        verify(bookRepository).findById(999L);
        verify(bookRepository, never()).save(any(Book.class));
    }
    
    @Test
    void testUploadBookImage_EmptyFile() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.jpg", "image/jpeg", new byte[0]);
        
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            advancedBookService.uploadBookImage(1L, file);
        });
        
        verify(bookRepository).findById(1L);
        verify(bookRepository, never()).save(any(Book.class));
    }
    
    @Test
    void testUploadBookImage_InvalidFileType() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.txt", "text/plain", "test content".getBytes());
        
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            advancedBookService.uploadBookImage(1L, file);
        });
        
        verify(bookRepository).findById(1L);
        verify(bookRepository, never()).save(any(Book.class));
    }
    
    @Test
    void testDeleteBookImage_Success() throws IOException {
        // Arrange
        testBook.setImageUrl("/api/books/1/image/book_1_123456789.jpg");
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);
        
        // Act
        advancedBookService.deleteBookImage(1L);
        
        // Assert
        verify(bookRepository).findById(1L);
        verify(bookRepository).save(any(Book.class));
    }
    
    @Test
    void testDeleteBookImage_BookNotFound() {
        // Arrange
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(BookNotFoundException.class, () -> {
            advancedBookService.deleteBookImage(999L);
        });
        
        verify(bookRepository).findById(999L);
        verify(bookRepository, never()).save(any(Book.class));
    }
}