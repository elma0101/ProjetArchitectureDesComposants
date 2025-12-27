package com.bookstore.recommendation.client;

import com.bookstore.recommendation.dto.BookResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(
    name = "book-catalog-service",
    fallback = BookCatalogClientFallback.class
)
public interface BookCatalogClient {
    
    @GetMapping("/api/books/{id}")
    BookResponse getBookById(@PathVariable("id") Long id);
    
    @GetMapping("/api/books")
    List<BookResponse> getAllBooks();
}
