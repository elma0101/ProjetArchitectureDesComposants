package com.bookstore.catalog.controller;

import com.bookstore.catalog.dto.*;
import com.bookstore.catalog.service.BookService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for book catalog operations
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * Create a new book
     */
    @PostMapping
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookRequest request) {
        logger.info("POST /api/books - Creating book with ISBN: {}", request.getIsbn());
        BookResponse response = bookService.createBook(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get book by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(@PathVariable Long id) {
        logger.debug("GET /api/books/{} - Fetching book", id);
        BookResponse response = bookService.getBookById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all books
     */
    @GetMapping
    public ResponseEntity<List<BookResponse>> getAllBooks(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean available) {
        
        logger.debug("GET /api/books - Fetching books with filters: genre={}, authorId={}, search={}, available={}", 
                    genre, authorId, search, available);

        List<BookResponse> books;

        if (search != null && !search.trim().isEmpty()) {
            books = bookService.searchBooks(search);
        } else if (genre != null && !genre.trim().isEmpty()) {
            books = bookService.findBooksByGenre(genre);
        } else if (authorId != null) {
            books = bookService.findBooksByAuthor(authorId);
        } else if (Boolean.TRUE.equals(available)) {
            books = bookService.findAvailableBooks();
        } else {
            books = bookService.getAllBooks();
        }

        return ResponseEntity.ok(books);
    }

    /**
     * Get book by ISBN
     */
    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<BookResponse> getBookByIsbn(@PathVariable String isbn) {
        logger.debug("GET /api/books/isbn/{} - Fetching book", isbn);
        BookResponse response = bookService.getBookByIsbn(isbn);
        return ResponseEntity.ok(response);
    }

    /**
     * Update book
     */
    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookRequest request) {
        logger.info("PUT /api/books/{} - Updating book", id);
        BookResponse response = bookService.updateBook(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete book
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        logger.info("DELETE /api/books/{} - Deleting book", id);
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Check book availability
     */
    @GetMapping("/{id}/availability")
    public ResponseEntity<BookAvailabilityResponse> checkAvailability(@PathVariable Long id) {
        logger.debug("GET /api/books/{}/availability - Checking availability", id);
        BookAvailabilityResponse response = bookService.checkAvailability(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all genres
     */
    @GetMapping("/genres")
    public ResponseEntity<List<String>> getAllGenres() {
        logger.debug("GET /api/books/genres - Fetching all genres");
        List<String> genres = bookService.getAllGenres();
        return ResponseEntity.ok(genres);
    }

    /**
     * Bulk create books
     */
    @PostMapping("/bulk")
    public ResponseEntity<BulkOperationResult> bulkCreateBooks(@Valid @RequestBody BulkBookRequest request) {
        logger.info("POST /api/books/bulk - Bulk creating {} books", request.getBooks().size());
        BulkOperationResult result = bookService.bulkCreateBooks(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Bulk delete books
     */
    @DeleteMapping("/bulk")
    public ResponseEntity<BulkOperationResult> bulkDeleteBooks(@RequestBody List<Long> bookIds) {
        logger.info("DELETE /api/books/bulk - Bulk deleting {} books", bookIds.size());
        BulkOperationResult result = bookService.bulkDeleteBooks(bookIds);
        return ResponseEntity.ok(result);
    }
}
