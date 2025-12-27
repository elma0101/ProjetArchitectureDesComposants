package com.bookstore.loanmanagement.client;

import com.bookstore.loanmanagement.dto.BookResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(
    name = "book-catalog-service",
    fallback = BookCatalogClientFallback.class
)
public interface BookCatalogClient {

    @GetMapping("/api/books/{id}")
    BookResponse getBookById(@PathVariable("id") Long id);

    @GetMapping("/api/books/{id}/availability")
    BookResponse checkAvailability(@PathVariable("id") Long id);

    @PutMapping("/api/books/{id}/borrow")
    void borrowBook(@PathVariable("id") Long id);

    @PutMapping("/api/books/{id}/return")
    void returnBook(@PathVariable("id") Long id);
}
