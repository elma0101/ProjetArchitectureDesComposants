package com.bookstore.client;

import com.bookstore.dto.AuthorStatistics;
import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.hateoas.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

/**
 * Feign client for external author service communication
 */
@FeignClient(
    name = "author-service", 
    url = "${bookstore.external.author-service.url:http://localhost:8080}",
    configuration = FeignClientConfig.class
)
public interface AuthorClient {
    
    /**
     * Get all authors with pagination
     */
    @GetMapping("/api/authors")
    PagedModel<Author> getAllAuthors(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    );
    
    /**
     * Get a specific author by ID
     */
    @GetMapping("/api/authors/{id}")
    Author getAuthorById(@PathVariable Long id);
    
    /**
     * Get books by author
     */
    @GetMapping("/api/authors/{authorId}/books")
    Set<Book> getBooksByAuthor(@PathVariable Long authorId);
    
    /**
     * Get author statistics
     */
    @GetMapping("/api/authors/{authorId}/statistics")
    AuthorStatistics getAuthorStatistics(@PathVariable Long authorId);
    
    /**
     * Get most prolific authors
     */
    @GetMapping("/api/authors/prolific")
    PagedModel<Author> getMostProlificAuthors(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    );
    
    /**
     * Get recently added authors
     */
    @GetMapping("/api/authors/recent")
    PagedModel<Author> getRecentlyAddedAuthors(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    );
    
    /**
     * Find authors by nationality
     */
    @GetMapping("/api/authors/nationality/{nationality}")
    PagedModel<Author> findAuthorsByNationality(
        @PathVariable String nationality,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    );
    
    /**
     * Find authors by birth year
     */
    @GetMapping("/api/authors/birth-year/{year}")
    PagedModel<Author> findAuthorsByBirthYear(
        @PathVariable Integer year,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    );
    
    /**
     * Search authors by name (Spring Data REST endpoint)
     */
    @GetMapping("/api/authors/search/findByName")
    PagedModel<Author> findAuthorsByName(@RequestParam String name);
    
    /**
     * Search authors by first name (Spring Data REST endpoint)
     */
    @GetMapping("/api/authors/search/findByFirstName")
    PagedModel<Author> findAuthorsByFirstName(@RequestParam String firstName);
    
    /**
     * Search authors by last name (Spring Data REST endpoint)
     */
    @GetMapping("/api/authors/search/findByLastName")
    PagedModel<Author> findAuthorsByLastName(@RequestParam String lastName);
}