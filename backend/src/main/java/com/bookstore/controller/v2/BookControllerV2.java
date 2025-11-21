package com.bookstore.controller.v2;

import com.bookstore.dto.BookCreateRequest;
import com.bookstore.dto.BookUpdateRequest;
import com.bookstore.dto.BulkBookRequest;
import com.bookstore.dto.BulkOperationResult;
import com.bookstore.entity.Book;
import com.bookstore.repository.BookRepository;
import com.bookstore.service.AdvancedBookService;
import com.bookstore.versioning.ApiVersion;
import com.bookstore.versioning.FeatureToggle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Book Controller Version 2.0 - Enhanced with DTOs and advanced features
 */
@RestController
@RequestMapping("/api/v2/books")
@ApiVersion("2.0")
public class BookControllerV2 {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AdvancedBookService advancedBookService;

    @GetMapping
    public ResponseEntity<Page<Book>> getAllBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) Boolean available,
            Pageable pageable) {
        
        // For V2, we use enhanced filtering through repository
        Page<Book> books = bookRepository.findAll(pageable);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return bookRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Book> createBook(@Valid @RequestBody BookCreateRequest request) {
        // Create book using the existing method pattern
        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setIsbn(request.getIsbn());
        book.setDescription(request.getDescription());
        book.setPublicationYear(request.getPublicationYear());
        book.setGenre(request.getGenre());
        book.setAvailableCopies(request.getAvailableCopies());
        book.setTotalCopies(request.getTotalCopies());
        
        Book savedBook = bookRepository.save(book);
        return ResponseEntity.ok(savedBook);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @Valid @RequestBody BookUpdateRequest request) {
        return bookRepository.findById(id)
                .map(book -> {
                    if (request.getTitle() != null) book.setTitle(request.getTitle());
                    if (request.getDescription() != null) book.setDescription(request.getDescription());
                    if (request.getPublicationYear() != null) book.setPublicationYear(request.getPublicationYear());
                    if (request.getGenre() != null) book.setGenre(request.getGenre());
                    if (request.getAvailableCopies() != null) book.setAvailableCopies(request.getAvailableCopies());
                    if (request.getTotalCopies() != null) book.setTotalCopies(request.getTotalCopies());
                    
                    Book updatedBook = bookRepository.save(book);
                    return ResponseEntity.ok(updatedBook);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        if (!bookRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        bookRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk")
    @FeatureToggle("bulk-operations")
    public ResponseEntity<BulkOperationResult<Book>> createBooksInBulk(@Valid @RequestBody BulkBookRequest request) {
        BulkOperationResult<Book> result = advancedBookService.bulkCreateBooks(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    @FeatureToggle("enhanced-search")
    public ResponseEntity<Page<Book>> searchBooks(
            @RequestParam String query,
            @RequestParam(required = false) String sortBy,
            Pageable pageable) {
        
        // Simple search implementation for demonstration
        Page<Book> books = bookRepository.findByTitleContainingIgnoreCase(query, pageable);
        return ResponseEntity.ok(books);
    }
}