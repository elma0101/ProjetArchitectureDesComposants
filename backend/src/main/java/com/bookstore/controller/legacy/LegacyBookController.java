package com.bookstore.controller.legacy;

import com.bookstore.entity.Book;
import com.bookstore.repository.BookRepository;
import com.bookstore.versioning.ApiVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Legacy Book Controller - Deprecated
 */
@RestController
@RequestMapping("/api/legacy/books")
@ApiVersion(value = "0.9", deprecated = true, 
           deprecatedSince = "2024-01-01", 
           migrationGuide = "https://docs.bookstore.com/migration/v1")
public class LegacyBookController {

    @Autowired
    private BookRepository bookRepository;

    @GetMapping
    @ApiVersion(value = "0.9", deprecated = true, 
               deprecatedSince = "2024-01-01", 
               migrationGuide = "Use /api/v1/books or /api/v2/books instead")
    public ResponseEntity<List<Book>> getAllBooks() {
        List<Book> books = bookRepository.findAll();
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return bookRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}