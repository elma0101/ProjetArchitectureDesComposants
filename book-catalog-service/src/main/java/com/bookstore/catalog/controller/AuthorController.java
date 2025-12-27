package com.bookstore.catalog.controller;

import com.bookstore.catalog.dto.AuthorRequest;
import com.bookstore.catalog.dto.AuthorResponse;
import com.bookstore.catalog.service.AuthorService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for author management operations
 */
@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    private static final Logger logger = LoggerFactory.getLogger(AuthorController.class);

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    /**
     * Create a new author
     */
    @PostMapping
    public ResponseEntity<AuthorResponse> createAuthor(@Valid @RequestBody AuthorRequest request) {
        logger.info("POST /api/authors - Creating author: {} {}", 
                   request.getFirstName(), request.getLastName());
        AuthorResponse response = authorService.createAuthor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get author by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<AuthorResponse> getAuthorById(@PathVariable Long id) {
        logger.debug("GET /api/authors/{} - Fetching author", id);
        AuthorResponse response = authorService.getAuthorById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all authors
     */
    @GetMapping
    public ResponseEntity<List<AuthorResponse>> getAllAuthors(
            @RequestParam(required = false) String nationality,
            @RequestParam(required = false) String search) {
        
        logger.debug("GET /api/authors - Fetching authors with filters: nationality={}, search={}", 
                    nationality, search);

        List<AuthorResponse> authors;

        if (search != null && !search.trim().isEmpty()) {
            authors = authorService.searchAuthors(search);
        } else if (nationality != null && !nationality.trim().isEmpty()) {
            authors = authorService.findAuthorsByNationality(nationality);
        } else {
            authors = authorService.getAllAuthors();
        }

        return ResponseEntity.ok(authors);
    }

    /**
     * Update author
     */
    @PutMapping("/{id}")
    public ResponseEntity<AuthorResponse> updateAuthor(
            @PathVariable Long id,
            @Valid @RequestBody AuthorRequest request) {
        logger.info("PUT /api/authors/{} - Updating author", id);
        AuthorResponse response = authorService.updateAuthor(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete author
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable Long id) {
        logger.info("DELETE /api/authors/{} - Deleting author", id);
        authorService.deleteAuthor(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get authors by book
     */
    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<AuthorResponse>> getAuthorsByBook(@PathVariable Long bookId) {
        logger.debug("GET /api/authors/book/{} - Fetching authors for book", bookId);
        List<AuthorResponse> authors = authorService.findAuthorsByBook(bookId);
        return ResponseEntity.ok(authors);
    }
}
