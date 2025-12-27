package com.bookstore.controller;

import com.bookstore.client.ExternalAuthorService;
import com.bookstore.client.ExternalBookService;
import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

/**
 * Controller demonstrating external service integration via FeignClient
 */
@RestController
@RequestMapping("/api/external")
@Tag(name = "External Services", description = "Integration with external book and author services")
@Profile("!h2")
public class ExternalServiceController {

    @Autowired
    private ExternalBookService externalBookService;

    @Autowired
    private ExternalAuthorService externalAuthorService;

    @GetMapping("/books")
    @Operation(summary = "Get books from external service", description = "Retrieve books from external service with circuit breaker protection")
    public ResponseEntity<PagedModel<Book>> getExternalBooks(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        try {
            PagedModel<Book> books = externalBookService.getAllBooks(page, size).get();
            return ResponseEntity.ok(books);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/books/{id}")
    @Operation(summary = "Get book by ID from external service", description = "Retrieve a specific book from external service")
    public ResponseEntity<Book> getExternalBookById(
            @Parameter(description = "Book ID") @PathVariable Long id) {
        
        try {
            Book book = externalBookService.getBookById(id).get();
            return book != null ? ResponseEntity.ok(book) : ResponseEntity.notFound().build();
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/books/search")
    @Operation(summary = "Search books in external service", description = "Search books by title in external service")
    public ResponseEntity<PagedModel<Book>> searchExternalBooks(
            @Parameter(description = "Book title to search") @RequestParam String title) {
        
        try {
            PagedModel<Book> books = externalBookService.findBooksByTitle(title).get();
            return ResponseEntity.ok(books);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/authors")
    @Operation(summary = "Get authors from external service", description = "Retrieve authors from external service with circuit breaker protection")
    public ResponseEntity<PagedModel<Author>> getExternalAuthors(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        try {
            PagedModel<Author> authors = externalAuthorService.getAllAuthors(page, size).get();
            return ResponseEntity.ok(authors);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/authors/{id}")
    @Operation(summary = "Get author by ID from external service", description = "Retrieve a specific author from external service")
    public ResponseEntity<Author> getExternalAuthorById(
            @Parameter(description = "Author ID") @PathVariable Long id) {
        
        try {
            Author author = externalAuthorService.getAuthorById(id).get();
            return author != null ? ResponseEntity.ok(author) : ResponseEntity.notFound().build();
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/authors/search")
    @Operation(summary = "Search authors in external service", description = "Search authors by name in external service")
    public ResponseEntity<PagedModel<Author>> searchExternalAuthors(
            @Parameter(description = "Author name to search") @RequestParam String name) {
        
        try {
            PagedModel<Author> authors = externalAuthorService.findAuthorsByName(name).get();
            return ResponseEntity.ok(authors);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }
}