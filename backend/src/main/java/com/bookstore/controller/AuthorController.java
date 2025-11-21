package com.bookstore.controller;

import com.bookstore.dto.*;
import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import com.bookstore.service.AuthorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/authors")
@Tag(name = "Author Management", description = "APIs for managing authors and their relationships with books")
public class AuthorController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthorController.class);
    
    private final AuthorService authorService;
    
    @Autowired
    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }
    
    @Operation(summary = "Create a new author", description = "Creates a new author with the provided information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Author created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Author.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "Author already exists")
    })
    @PostMapping
    public ResponseEntity<Author> createAuthor(@Valid @RequestBody AuthorCreateRequest request) {
        logger.info("Creating new author: {} {}", request.getFirstName(), request.getLastName());
        Author createdAuthor = authorService.createAuthor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAuthor);
    }
    
    @Operation(summary = "Update an existing author", description = "Updates an author's information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Author updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Author.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Author not found")
    })
    @PutMapping("/{authorId}")
    public ResponseEntity<Author> updateAuthor(
            @Parameter(description = "Author ID", required = true) @PathVariable Long authorId,
            @Valid @RequestBody AuthorUpdateRequest request) {
        logger.info("Updating author with ID: {}", authorId);
        Author updatedAuthor = authorService.updateAuthor(authorId, request);
        return ResponseEntity.ok(updatedAuthor);
    }
    
    @Operation(summary = "Get author by ID", description = "Retrieves an author by their ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Author found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Author.class))),
        @ApiResponse(responseCode = "404", description = "Author not found")
    })
    @GetMapping("/{authorId}")
    public ResponseEntity<Author> getAuthorById(
            @Parameter(description = "Author ID", required = true) @PathVariable Long authorId) {
        Author author = authorService.getAuthorById(authorId);
        return ResponseEntity.ok(author);
    }
    
    @Operation(summary = "Get all authors", description = "Retrieves all authors with pagination")
    @ApiResponse(responseCode = "200", description = "Authors retrieved successfully")
    @GetMapping
    public ResponseEntity<Page<Author>> getAllAuthors(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Author> authors = authorService.getAllAuthors(pageable);
        return ResponseEntity.ok(authors);
    }
    
    @Operation(summary = "Delete an author", description = "Deletes an author and removes their associations with books")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Author deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Author not found")
    })
    @DeleteMapping("/{authorId}")
    public ResponseEntity<Void> deleteAuthor(
            @Parameter(description = "Author ID", required = true) @PathVariable Long authorId) {
        logger.info("Deleting author with ID: {}", authorId);
        authorService.deleteAuthor(authorId);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(summary = "Add books to author", description = "Associates books with an author")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Books added to author successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Author.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Author or book not found")
    })
    @PostMapping("/{authorId}/books")
    public ResponseEntity<Author> addBooksToAuthor(
            @Parameter(description = "Author ID", required = true) @PathVariable Long authorId,
            @Valid @RequestBody AuthorBookAssociationRequest request) {
        logger.info("Adding books to author with ID: {}", authorId);
        Author updatedAuthor = authorService.addBooksToAuthor(authorId, request);
        return ResponseEntity.ok(updatedAuthor);
    }
    
    @Operation(summary = "Remove books from author", description = "Removes book associations from an author")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Books removed from author successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Author.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Author or book not found")
    })
    @DeleteMapping("/{authorId}/books")
    public ResponseEntity<Author> removeBooksFromAuthor(
            @Parameter(description = "Author ID", required = true) @PathVariable Long authorId,
            @Valid @RequestBody AuthorBookAssociationRequest request) {
        logger.info("Removing books from author with ID: {}", authorId);
        Author updatedAuthor = authorService.removeBooksFromAuthor(authorId, request);
        return ResponseEntity.ok(updatedAuthor);
    }
    
    @Operation(summary = "Set books for author", description = "Replaces all book associations for an author")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Books set for author successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Author.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Author or book not found")
    })
    @PutMapping("/{authorId}/books")
    public ResponseEntity<Author> setBooksForAuthor(
            @Parameter(description = "Author ID", required = true) @PathVariable Long authorId,
            @Valid @RequestBody AuthorBookAssociationRequest request) {
        logger.info("Setting books for author with ID: {}", authorId);
        Author updatedAuthor = authorService.setBooksForAuthor(authorId, request);
        return ResponseEntity.ok(updatedAuthor);
    }
    
    @Operation(summary = "Get books by author", description = "Retrieves all books written by an author")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Books retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Author not found")
    })
    @GetMapping("/{authorId}/books")
    public ResponseEntity<Set<Book>> getBooksByAuthor(
            @Parameter(description = "Author ID", required = true) @PathVariable Long authorId) {
        Set<Book> books = authorService.getBooksByAuthor(authorId);
        return ResponseEntity.ok(books);
    }
    
    @Operation(summary = "Search authors", description = "Searches authors with multiple criteria")
    @ApiResponse(responseCode = "200", description = "Authors found")
    @PostMapping("/search")
    public ResponseEntity<Page<Author>> searchAuthors(
            @Valid @RequestBody AuthorSearchRequest request,
            @PageableDefault(size = 20) Pageable pageable) {
        logger.info("Searching authors with criteria: {}", request);
        Page<Author> authors = authorService.searchAuthors(request, pageable);
        return ResponseEntity.ok(authors);
    }
    
    @Operation(summary = "Get author statistics", description = "Retrieves statistics for an author")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthorStatistics.class))),
        @ApiResponse(responseCode = "404", description = "Author not found")
    })
    @GetMapping("/{authorId}/statistics")
    public ResponseEntity<AuthorStatistics> getAuthorStatistics(
            @Parameter(description = "Author ID", required = true) @PathVariable Long authorId) {
        AuthorStatistics statistics = authorService.getAuthorStatistics(authorId);
        return ResponseEntity.ok(statistics);
    }
    
    @Operation(summary = "Get most prolific authors", description = "Retrieves authors ordered by number of books written")
    @ApiResponse(responseCode = "200", description = "Authors retrieved successfully")
    @GetMapping("/prolific")
    public ResponseEntity<Page<Author>> getMostProlificAuthors(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Author> authors = authorService.getMostProlificAuthors(pageable);
        return ResponseEntity.ok(authors);
    }
    
    @Operation(summary = "Get recently added authors", description = "Retrieves recently added authors")
    @ApiResponse(responseCode = "200", description = "Authors retrieved successfully")
    @GetMapping("/recent")
    public ResponseEntity<Page<Author>> getRecentlyAddedAuthors(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Author> authors = authorService.getRecentlyAddedAuthors(pageable);
        return ResponseEntity.ok(authors);
    }
    
    @Operation(summary = "Find authors by nationality", description = "Finds authors by their nationality")
    @ApiResponse(responseCode = "200", description = "Authors found")
    @GetMapping("/nationality/{nationality}")
    public ResponseEntity<Page<Author>> findAuthorsByNationality(
            @Parameter(description = "Nationality", required = true) @PathVariable String nationality,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Author> authors = authorService.findAuthorsByNationality(nationality, pageable);
        return ResponseEntity.ok(authors);
    }
    
    @Operation(summary = "Find authors by birth year", description = "Finds authors by their birth year")
    @ApiResponse(responseCode = "200", description = "Authors found")
    @GetMapping("/birth-year/{year}")
    public ResponseEntity<Page<Author>> findAuthorsByBirthYear(
            @Parameter(description = "Birth year", required = true) @PathVariable Integer year,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Author> authors = authorService.findAuthorsByBirthYear(year, pageable);
        return ResponseEntity.ok(authors);
    }
}