package com.bookstore.client;

import com.bookstore.entity.Book;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client for external book service communication
 */
@FeignClient(
    name = "book-service", 
    url = "${bookstore.external.book-service.url:http://localhost:8080}",
    configuration = FeignClientConfig.class
)
public interface BookClient {
    
    /**
     * Get all books with pagination
     */
    @GetMapping("/api/books")
    PagedModel<Book> getAllBooks(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    );
    
    /**
     * Get a specific book by ID
     */
    @GetMapping("/api/books/{id}")
    Book getBookById(@PathVariable Long id);
    
    /**
     * Search books by title
     */
    @GetMapping("/api/books/search/findByTitle")
    PagedModel<Book> findBooksByTitle(@RequestParam String title);
    
    /**
     * Search books by author name
     */
    @GetMapping("/api/books/search/findByAuthor")
    PagedModel<Book> findBooksByAuthor(@RequestParam String author);
    
    /**
     * Search books by ISBN
     */
    @GetMapping("/api/books/search/findByIsbn")
    PagedModel<Book> findBooksByIsbn(@RequestParam String isbn);
    
    /**
     * Search books by genre
     */
    @GetMapping("/api/books/search/findByGenre")
    PagedModel<Book> findBooksByGenre(@RequestParam String genre);
    
    /**
     * Get books by a specific author ID
     */
    @GetMapping("/api/authors/{authorId}/books")
    PagedModel<Book> getBooksByAuthor(@PathVariable Long authorId);
}