package com.bookstore.repository;

import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BookRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private AuthorRepository authorRepository;
    
    private Book testBook1;
    private Book testBook2;
    private Author testAuthor;
    
    @BeforeEach
    void setUp() {
        // Create test author
        testAuthor = new Author("John", "Doe");
        testAuthor = entityManager.persistAndFlush(testAuthor);
        
        // Create test books
        testBook1 = new Book("Test Title 1", "978-0-452-28423-4");
        testBook1.setDescription("A test book description");
        testBook1.setPublicationYear(2023);
        testBook1.setGenre("Fiction");
        testBook1.setAvailableCopies(5);
        testBook1.setTotalCopies(10);
        testBook1.addAuthor(testAuthor);
        testBook1 = entityManager.persistAndFlush(testBook1);
        
        testBook2 = new Book("Another Book", "978-0-452-28424-1");
        testBook2.setDescription("Another test book");
        testBook2.setPublicationYear(2022);
        testBook2.setGenre("Mystery");
        testBook2.setAvailableCopies(0);
        testBook2.setTotalCopies(3);
        testBook2 = entityManager.persistAndFlush(testBook2);
        
        entityManager.clear();
    }
    
    @Test
    void testFindByTitleContainingIgnoreCase() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> result = bookRepository.findByTitleContainingIgnoreCase("test", pageable);
        
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Title 1", result.getContent().get(0).getTitle());
        
        // Test case insensitive
        result = bookRepository.findByTitleContainingIgnoreCase("TEST", pageable);
        assertEquals(1, result.getTotalElements());
    }
    
    @Test
    void testFindByIsbn() {
        Optional<Book> result = bookRepository.findByIsbn("978-0-452-28423-4");
        
        assertTrue(result.isPresent());
        assertEquals("Test Title 1", result.get().getTitle());
        
        Optional<Book> notFound = bookRepository.findByIsbn("non-existent-isbn");
        assertFalse(notFound.isPresent());
    }
    
    @Test
    void testFindByGenreContainingIgnoreCase() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> result = bookRepository.findByGenreContainingIgnoreCase("fiction", pageable);
        
        assertEquals(1, result.getTotalElements());
        assertEquals("Fiction", result.getContent().get(0).getGenre());
        
        // Test case insensitive
        result = bookRepository.findByGenreContainingIgnoreCase("FICTION", pageable);
        assertEquals(1, result.getTotalElements());
    }
    
    @Test
    void testFindByPublicationYear() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> result = bookRepository.findByPublicationYear(2023, pageable);
        
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Title 1", result.getContent().get(0).getTitle());
        
        result = bookRepository.findByPublicationYear(2024, pageable);
        assertEquals(0, result.getTotalElements());
    }
    
    @Test
    void testFindByPublicationYearBetween() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> result = bookRepository.findByPublicationYearBetween(2022, 2023, pageable);
        
        assertEquals(2, result.getTotalElements());
        
        result = bookRepository.findByPublicationYearBetween(2024, 2025, pageable);
        assertEquals(0, result.getTotalElements());
    }
    
    @Test
    void testFindAvailableBooks() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> result = bookRepository.findAvailableBooks(pageable);
        
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Title 1", result.getContent().get(0).getTitle());
        assertTrue(result.getContent().get(0).getAvailableCopies() > 0);
    }
    
    @Test
    void testFindByAuthorNameContainingIgnoreCase() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> result = bookRepository.findByAuthorNameContainingIgnoreCase("john doe", pageable);
        
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Title 1", result.getContent().get(0).getTitle());
        
        // Test partial name
        result = bookRepository.findByAuthorNameContainingIgnoreCase("john", pageable);
        assertEquals(1, result.getTotalElements());
        
        // Test case insensitive
        result = bookRepository.findByAuthorNameContainingIgnoreCase("JOHN DOE", pageable);
        assertEquals(1, result.getTotalElements());
    }
    
    @Test
    void testFindByAuthorId() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> result = bookRepository.findByAuthorId(testAuthor.getId(), pageable);
        
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Title 1", result.getContent().get(0).getTitle());
        
        result = bookRepository.findByAuthorId(999L, pageable);
        assertEquals(0, result.getTotalElements());
    }
    
    @Test
    void testSearchBooks() {
        Pageable pageable = PageRequest.of(0, 10);
        
        // Search by title only
        Page<Book> result = bookRepository.searchBooks("test", null, null, null, false, pageable);
        assertEquals(1, result.getTotalElements());
        
        // Search by genre only
        result = bookRepository.searchBooks(null, "fiction", null, null, false, pageable);
        assertEquals(1, result.getTotalElements());
        
        // Search by author name only
        result = bookRepository.searchBooks(null, null, "john", null, false, pageable);
        assertEquals(1, result.getTotalElements());
        
        // Search by publication year only
        result = bookRepository.searchBooks(null, null, null, 2023, false, pageable);
        assertEquals(1, result.getTotalElements());
        
        // Search available only
        result = bookRepository.searchBooks(null, null, null, null, true, pageable);
        assertEquals(1, result.getTotalElements());
        
        // Combined search
        result = bookRepository.searchBooks("test", "fiction", "john", 2023, true, pageable);
        assertEquals(1, result.getTotalElements());
        
        // No matches
        result = bookRepository.searchBooks("nonexistent", null, null, null, false, pageable);
        assertEquals(0, result.getTotalElements());
    }
    
    @Test
    void testFindBooksWithLowStock() {
        List<Book> result = bookRepository.findBooksWithLowStock(3);
        
        // Only testBook1 has available copies (5), testBook2 has 0
        assertEquals(0, result.size()); // No books with stock <= 3 and > 0
        
        result = bookRepository.findBooksWithLowStock(10);
        assertEquals(1, result.size());
        assertEquals("Test Title 1", result.get(0).getTitle());
    }
    
    @Test
    void testFindOutOfStockBooks() {
        List<Book> result = bookRepository.findOutOfStockBooks();
        
        assertEquals(1, result.size());
        assertEquals("Another Book", result.get(0).getTitle());
        assertEquals(0, result.get(0).getAvailableCopies().intValue());
        assertTrue(result.get(0).getTotalCopies() > 0);
    }
    
    @Test
    void testCountByGenre() {
        Long count = bookRepository.countByGenre("Fiction");
        assertEquals(1L, count);
        
        count = bookRepository.countByGenre("Mystery");
        assertEquals(1L, count);
        
        count = bookRepository.countByGenre("NonExistent");
        assertEquals(0L, count);
    }
    
    @Test
    void testExistsByIsbn() {
        assertTrue(bookRepository.existsByIsbn("978-0-452-28423-4"));
        assertFalse(bookRepository.existsByIsbn("non-existent-isbn"));
    }
    
    @Test
    void testFindByIsbnIn() {
        List<String> isbns = List.of("978-0-452-28423-4", "978-0-452-28424-1", "non-existent");
        List<Book> result = bookRepository.findByIsbnIn(isbns);
        
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(book -> book.getTitle().equals("Test Title 1")));
        assertTrue(result.stream().anyMatch(book -> book.getTitle().equals("Another Book")));
    }
    
    @Test
    void testFindMostPopularBooks() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> result = bookRepository.findMostPopularBooks(pageable);
        
        // Should return all books ordered by loan count (0 for both in this test)
        assertEquals(2, result.getTotalElements());
    }
    
    @Test
    void testFindRecentlyAddedBooks() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> result = bookRepository.findRecentlyAddedBooks(pageable);
        
        assertEquals(2, result.getTotalElements());
        // Should be ordered by creation date descending
        // The exact order depends on the persistence timing
    }
}