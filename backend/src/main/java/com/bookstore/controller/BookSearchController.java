package com.bookstore.controller;

import com.bookstore.entity.Book;
import com.bookstore.repository.BookRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Controller for advanced book search and filtering operations.
 * Provides endpoints for searching books by various criteria with pagination and sorting.
 */
@RestController
@RequestMapping("/api/books/search")
@Tag(name = "Search", description = "Advanced book search and filtering capabilities")
public class BookSearchController {

    private final BookRepository bookRepository;

    @Autowired
    public BookSearchController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * Search books by title with pagination and sorting.
     * Requirement 5.2: Search by title functionality
     */
    @GetMapping("/findByTitle")
    @Operation(
        summary = "Search books by title",
        description = "Search for books by title with case-insensitive partial matching. Supports pagination and sorting. Example: ?title=Java&page=0&size=10"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Books found successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid search parameters",
            content = @Content(schema = @Schema(hidden = true))
        )
    })
    public ResponseEntity<Page<Book>> findByTitle(
            @Parameter(description = "Title to search for (case-insensitive partial match)", required = true, example = "Java Programming")
            @RequestParam String title,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Number of items per page", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @Parameter(description = "Field to sort by", example = "title", schema = @Schema(allowableValues = {"title", "publicationYear", "genre", "availableCopies", "createdAt"}))
            @RequestParam(defaultValue = "title") String sortBy,
            @Parameter(description = "Sort direction", example = "asc", schema = @Schema(allowableValues = {"asc", "desc"}))
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = createSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Book> books = bookRepository.findByTitleContainingIgnoreCase(title, pageable);
        return ResponseEntity.ok(books);
    }

    /**
     * Search books by author name with pagination and sorting.
     * Requirement 5.2: Search by author functionality
     */
    @GetMapping("/findByAuthor")
    @Operation(
        summary = "Search books by author name",
        description = "Search for books by author name with case-insensitive partial matching. Supports pagination and sorting."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Books found successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid search parameters")
    })
    public ResponseEntity<Page<Book>> findByAuthor(
            @Parameter(description = "Author name to search for (case-insensitive partial match)", required = true, example = "Robert Martin")
            @RequestParam String author,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Number of items per page", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @Parameter(description = "Field to sort by", example = "title")
            @RequestParam(defaultValue = "title") String sortBy,
            @Parameter(description = "Sort direction", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = createSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Book> books = bookRepository.findByAuthorNameContainingIgnoreCase(author, pageable);
        return ResponseEntity.ok(books);
    }

    /**
     * Search books by ISBN.
     * Requirement 5.2: Search by ISBN functionality
     */
    @GetMapping("/findByIsbn")
    @Operation(
        summary = "Find book by ISBN",
        description = "Find a specific book by its ISBN. Returns a single book or 404 if not found."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Book found successfully"),
        @ApiResponse(responseCode = "404", description = "Book not found"),
        @ApiResponse(responseCode = "400", description = "Invalid ISBN format")
    })
    public ResponseEntity<Book> findByIsbn(
            @Parameter(description = "ISBN of the book to find", required = true, example = "978-0134685991")
            @RequestParam String isbn) {
        Optional<Book> book = bookRepository.findByIsbn(isbn);
        return book.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Search books by genre with pagination and sorting.
     * Requirement 5.2: Search by genre functionality
     */
    @GetMapping("/findByGenre")
    public ResponseEntity<Page<Book>> findByGenre(
            @RequestParam String genre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = createSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Book> books = bookRepository.findByGenreContainingIgnoreCase(genre, pageable);
        return ResponseEntity.ok(books);
    }

    /**
     * Advanced search with multiple criteria and filters.
     * Requirements 5.1, 5.3, 5.4: Advanced search with filtering and sorting
     */
    @GetMapping("/advanced")
    @Operation(
        summary = "Advanced book search",
        description = "Search books using multiple criteria including title, author, genre, publication year, and availability. All search parameters are optional and can be combined. Example: ?title=Java&publicationYear=2020&availableOnly=true"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid search parameters")
    })
    public ResponseEntity<Page<Book>> advancedSearch(
            @Parameter(description = "Title to search for (optional)", example = "Java")
            @RequestParam(required = false) String title,
            @Parameter(description = "Author name to search for (optional)", example = "Robert Martin")
            @RequestParam(required = false) String author,
            @Parameter(description = "Genre to filter by (optional)", example = "Programming")
            @RequestParam(required = false) String genre,
            @Parameter(description = "Publication year to filter by (optional)", example = "2020")
            @RequestParam(required = false) Integer publicationYear,
            @Parameter(description = "Filter to show only available books", example = "true")
            @RequestParam(required = false, defaultValue = "false") boolean availableOnly,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Number of items per page", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @Parameter(description = "Field to sort by", example = "title")
            @RequestParam(defaultValue = "title") String sortBy,
            @Parameter(description = "Sort direction", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = createSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Book> books = bookRepository.searchBooks(
            title, genre, author, publicationYear, availableOnly, pageable);
        
        return ResponseEntity.ok(books);
    }

    /**
     * Search books by publication year with pagination and sorting.
     * Requirement 5.3: Filtering by publication year
     */
    @GetMapping("/findByYear")
    public ResponseEntity<Page<Book>> findByPublicationYear(
            @RequestParam Integer year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = createSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Book> books = bookRepository.findByPublicationYear(year, pageable);
        return ResponseEntity.ok(books);
    }

    /**
     * Search books by publication year range with pagination and sorting.
     * Requirement 5.3: Filtering by publication year range
     */
    @GetMapping("/findByYearRange")
    public ResponseEntity<Page<Book>> findByPublicationYearRange(
            @RequestParam Integer startYear,
            @RequestParam Integer endYear,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = createSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Book> books = bookRepository.findByPublicationYearBetween(startYear, endYear, pageable);
        return ResponseEntity.ok(books);
    }

    /**
     * Find available books (books with available copies > 0).
     * Requirement 5.3: Filtering by availability
     */
    @GetMapping("/available")
    public ResponseEntity<Page<Book>> findAvailableBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = createSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Book> books = bookRepository.findAvailableBooks(pageable);
        return ResponseEntity.ok(books);
    }

    /**
     * Find most popular books (books with most loans).
     * Requirement 5.4: Sorting by relevance/popularity
     */
    @GetMapping("/popular")
    public ResponseEntity<Page<Book>> findPopularBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> books = bookRepository.findMostPopularBooks(pageable);
        return ResponseEntity.ok(books);
    }

    /**
     * Find recently added books.
     * Requirement 5.4: Sorting by date added
     */
    @GetMapping("/recent")
    public ResponseEntity<Page<Book>> findRecentBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> books = bookRepository.findRecentlyAddedBooks(pageable);
        return ResponseEntity.ok(books);
    }

    /**
     * Helper method to create Sort object based on sortBy and sortDir parameters.
     * Requirement 5.4: Sorting support
     */
    private Sort createSort(String sortBy, String sortDir) {
        // Validate sortBy parameter to prevent injection attacks
        String validatedSortBy = validateSortField(sortBy);
        
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) 
            ? Sort.Direction.DESC 
            : Sort.Direction.ASC;
            
        return Sort.by(direction, validatedSortBy);
    }

    /**
     * Validate sort field to ensure it's a valid Book entity field.
     * Prevents potential security issues with dynamic sorting.
     */
    private String validateSortField(String sortBy) {
        switch (sortBy.toLowerCase()) {
            case "title":
                return "title";
            case "publicationyear":
            case "publication_year":
                return "publicationYear";
            case "genre":
                return "genre";
            case "availablecopies":
            case "available_copies":
                return "availableCopies";
            case "totalcopies":
            case "total_copies":
                return "totalCopies";
            case "createdat":
            case "created_at":
                return "createdAt";
            case "updatedat":
            case "updated_at":
                return "updatedAt";
            case "isbn":
                return "isbn";
            default:
                return "title"; // Default fallback
        }
    }
}