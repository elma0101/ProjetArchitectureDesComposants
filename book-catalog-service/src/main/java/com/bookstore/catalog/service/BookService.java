package com.bookstore.catalog.service;

import com.bookstore.catalog.dto.*;
import com.bookstore.catalog.entity.Author;
import com.bookstore.catalog.entity.Book;
import com.bookstore.catalog.event.*;
import com.bookstore.catalog.exception.AuthorNotFoundException;
import com.bookstore.catalog.exception.BookNotFoundException;
import com.bookstore.catalog.exception.DuplicateIsbnException;
import com.bookstore.catalog.exception.InvalidBookOperationException;
import com.bookstore.catalog.repository.AuthorRepository;
import com.bookstore.catalog.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing books in the catalog
 */
@Service
@Transactional
public class BookService {

    private static final Logger logger = LoggerFactory.getLogger(BookService.class);

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final BookEventPublisher eventPublisher;

    public BookService(BookRepository bookRepository, AuthorRepository authorRepository, 
                      BookEventPublisher eventPublisher) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Create a new book
     */
    public BookResponse createBook(BookRequest request) {
        logger.info("Creating book with ISBN: {}", request.getIsbn());

        // Check for duplicate ISBN
        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new DuplicateIsbnException(request.getIsbn());
        }

        // Validate copy counts
        if (request.getAvailableCopies() != null && request.getTotalCopies() != null) {
            if (request.getAvailableCopies() > request.getTotalCopies()) {
                throw new InvalidBookOperationException("Available copies cannot exceed total copies");
            }
        }

        Book book = mapToEntity(request);

        // Add authors if provided
        if (request.getAuthorIds() != null && !request.getAuthorIds().isEmpty()) {
            Set<Author> authors = new HashSet<>();
            for (Long authorId : request.getAuthorIds()) {
                Author author = authorRepository.findById(authorId)
                    .orElseThrow(() -> new AuthorNotFoundException(authorId));
                authors.add(author);
            }
            book.setAuthors(authors);
        }

        Book savedBook = bookRepository.save(book);
        logger.info("Book created successfully with ID: {}", savedBook.getId());

        // Publish book created event
        publishBookCreatedEvent(savedBook);

        return mapToResponse(savedBook);
    }

    /**
     * Get book by ID
     */
    @Transactional(readOnly = true)
    public BookResponse getBookById(Long id) {
        logger.debug("Fetching book with ID: {}", id);
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new BookNotFoundException(id));
        return mapToResponse(book);
    }

    /**
     * Get book by ISBN
     */
    @Transactional(readOnly = true)
    public BookResponse getBookByIsbn(String isbn) {
        logger.debug("Fetching book with ISBN: {}", isbn);
        Book book = bookRepository.findByIsbn(isbn)
            .orElseThrow(() -> new BookNotFoundException("isbn", isbn));
        return mapToResponse(book);
    }

    /**
     * Get all books
     */
    @Transactional(readOnly = true)
    public List<BookResponse> getAllBooks() {
        logger.debug("Fetching all books");
        return bookRepository.findAll().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Update book
     */
    public BookResponse updateBook(Long id, BookRequest request) {
        logger.info("Updating book with ID: {}", id);

        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new BookNotFoundException(id));

        // Check if ISBN is being changed and if new ISBN already exists
        if (!book.getIsbn().equals(request.getIsbn())) {
            if (bookRepository.existsByIsbn(request.getIsbn())) {
                throw new DuplicateIsbnException(request.getIsbn());
            }
        }

        // Validate copy counts
        if (request.getAvailableCopies() != null && request.getTotalCopies() != null) {
            if (request.getAvailableCopies() > request.getTotalCopies()) {
                throw new InvalidBookOperationException("Available copies cannot exceed total copies");
            }
        }

        // Store previous available copies for event
        Integer previousAvailableCopies = book.getAvailableCopies();

        // Update fields
        book.setTitle(request.getTitle());
        book.setIsbn(request.getIsbn());
        book.setDescription(request.getDescription());
        book.setPublicationYear(request.getPublicationYear());
        book.setGenre(request.getGenre());
        book.setAvailableCopies(request.getAvailableCopies());
        book.setTotalCopies(request.getTotalCopies());
        book.setImageUrl(request.getImageUrl());

        // Update authors if provided
        if (request.getAuthorIds() != null) {
            book.getAuthors().clear();
            for (Long authorId : request.getAuthorIds()) {
                Author author = authorRepository.findById(authorId)
                    .orElseThrow(() -> new AuthorNotFoundException(authorId));
                book.addAuthor(author);
            }
        }

        Book updatedBook = bookRepository.save(book);
        logger.info("Book updated successfully with ID: {}", updatedBook.getId());

        // Publish book updated event
        publishBookUpdatedEvent(updatedBook, previousAvailableCopies);

        return mapToResponse(updatedBook);
    }

    /**
     * Delete book
     */
    public void deleteBook(Long id) {
        logger.info("Deleting book with ID: {}", id);

        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new BookNotFoundException(id));

        // Publish book deleted event before deletion
        publishBookDeletedEvent(book);

        bookRepository.delete(book);
        logger.info("Book deleted successfully with ID: {}", id);
    }

    /**
     * Search books by keyword
     */
    @Transactional(readOnly = true)
    public List<BookResponse> searchBooks(String keyword) {
        logger.debug("Searching books with keyword: {}", keyword);
        return bookRepository.searchBooks(keyword).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Find books by genre
     */
    @Transactional(readOnly = true)
    public List<BookResponse> findBooksByGenre(String genre) {
        logger.debug("Finding books by genre: {}", genre);
        return bookRepository.findByGenreIgnoreCase(genre).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Find books by author
     */
    @Transactional(readOnly = true)
    public List<BookResponse> findBooksByAuthor(Long authorId) {
        logger.debug("Finding books by author ID: {}", authorId);
        
        // Verify author exists
        if (!authorRepository.existsById(authorId)) {
            throw new AuthorNotFoundException(authorId);
        }

        return bookRepository.findByAuthorId(authorId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Find available books
     */
    @Transactional(readOnly = true)
    public List<BookResponse> findAvailableBooks() {
        logger.debug("Finding available books");
        return bookRepository.findAvailableBooks().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Check book availability
     */
    @Transactional(readOnly = true)
    public BookAvailabilityResponse checkAvailability(Long id) {
        logger.debug("Checking availability for book ID: {}", id);

        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new BookNotFoundException(id));

        return new BookAvailabilityResponse(
            book.getId(),
            book.getTitle(),
            book.getIsbn(),
            book.isAvailable(),
            book.getAvailableCopies(),
            book.getTotalCopies()
        );
    }

    /**
     * Get all genres
     */
    @Transactional(readOnly = true)
    public List<String> getAllGenres() {
        logger.debug("Fetching all genres");
        return bookRepository.findAllGenres();
    }

    /**
     * Bulk create books
     */
    public BulkOperationResult bulkCreateBooks(BulkBookRequest request) {
        logger.info("Bulk creating {} books", request.getBooks().size());

        BulkOperationResult result = new BulkOperationResult(request.getBooks().size());

        for (BookRequest bookRequest : request.getBooks()) {
            try {
                BookResponse created = createBook(bookRequest);
                result.addSuccess(created.getId());
            } catch (Exception e) {
                logger.error("Failed to create book with ISBN: {}", bookRequest.getIsbn(), e);
                result.addError("ISBN " + bookRequest.getIsbn() + ": " + e.getMessage());
            }
        }

        logger.info("Bulk create completed: {} successful, {} failed", 
                   result.getSuccessCount(), result.getFailureCount());

        return result;
    }

    /**
     * Bulk delete books
     */
    public BulkOperationResult bulkDeleteBooks(List<Long> bookIds) {
        logger.info("Bulk deleting {} books", bookIds.size());

        BulkOperationResult result = new BulkOperationResult(bookIds.size());

        for (Long bookId : bookIds) {
            try {
                deleteBook(bookId);
                result.addSuccess(bookId);
            } catch (Exception e) {
                logger.error("Failed to delete book with ID: {}", bookId, e);
                result.addError("Book ID " + bookId + ": " + e.getMessage());
            }
        }

        logger.info("Bulk delete completed: {} successful, {} failed", 
                   result.getSuccessCount(), result.getFailureCount());

        return result;
    }

    // Mapping methods

    private Book mapToEntity(BookRequest request) {
        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setIsbn(request.getIsbn());
        book.setDescription(request.getDescription());
        book.setPublicationYear(request.getPublicationYear());
        book.setGenre(request.getGenre());
        book.setAvailableCopies(request.getAvailableCopies() != null ? request.getAvailableCopies() : 0);
        book.setTotalCopies(request.getTotalCopies() != null ? request.getTotalCopies() : 0);
        book.setImageUrl(request.getImageUrl());
        return book;
    }

    private BookResponse mapToResponse(Book book) {
        BookResponse response = new BookResponse();
        response.setId(book.getId());
        response.setTitle(book.getTitle());
        response.setIsbn(book.getIsbn());
        response.setDescription(book.getDescription());
        response.setPublicationYear(book.getPublicationYear());
        response.setGenre(book.getGenre());
        response.setAvailableCopies(book.getAvailableCopies());
        response.setTotalCopies(book.getTotalCopies());
        response.setImageUrl(book.getImageUrl());
        response.setCreatedAt(book.getCreatedAt());
        response.setUpdatedAt(book.getUpdatedAt());

        // Map authors (without books to avoid circular reference)
        if (book.getAuthors() != null) {
            Set<AuthorResponse> authorResponses = book.getAuthors().stream()
                .map(this::mapAuthorToResponse)
                .collect(Collectors.toSet());
            response.setAuthors(authorResponses);
        }

        return response;
    }

    private AuthorResponse mapAuthorToResponse(Author author) {
        AuthorResponse response = new AuthorResponse();
        response.setId(author.getId());
        response.setFirstName(author.getFirstName());
        response.setLastName(author.getLastName());
        response.setBiography(author.getBiography());
        response.setBirthDate(author.getBirthDate());
        response.setNationality(author.getNationality());
        response.setCreatedAt(author.getCreatedAt());
        response.setUpdatedAt(author.getUpdatedAt());
        response.setBookCount(author.getBooks() != null ? author.getBooks().size() : 0);
        return response;
    }

    // Event publishing methods

    private void publishBookCreatedEvent(Book book) {
        try {
            Set<Long> authorIds = book.getAuthors() != null 
                ? book.getAuthors().stream().map(Author::getId).collect(Collectors.toSet())
                : new HashSet<>();

            BookCreatedEvent event = new BookCreatedEvent(
                book.getId(),
                book.getIsbn(),
                book.getTitle(),
                book.getDescription(),
                book.getPublicationYear(),
                book.getGenre(),
                book.getTotalCopies(),
                book.getAvailableCopies(),
                authorIds,
                null // correlationId will be set by publisher
            );

            eventPublisher.publishBookCreated(event);
        } catch (Exception e) {
            logger.error("Failed to publish book created event for book ID: {}", book.getId(), e);
            // Don't fail the operation if event publishing fails
        }
    }

    private void publishBookUpdatedEvent(Book book, Integer previousAvailableCopies) {
        try {
            Set<Long> authorIds = book.getAuthors() != null 
                ? book.getAuthors().stream().map(Author::getId).collect(Collectors.toSet())
                : new HashSet<>();

            BookUpdatedEvent event = new BookUpdatedEvent(
                book.getId(),
                book.getIsbn(),
                book.getTitle(),
                book.getDescription(),
                book.getPublicationYear(),
                book.getGenre(),
                book.getTotalCopies(),
                book.getAvailableCopies(),
                authorIds,
                previousAvailableCopies,
                null // correlationId will be set by publisher
            );

            eventPublisher.publishBookUpdated(event);

            // Check if availability changed and publish separate event
            if (previousAvailableCopies != null && 
                !previousAvailableCopies.equals(book.getAvailableCopies())) {
                publishBookAvailabilityChangedEvent(
                    book, 
                    previousAvailableCopies, 
                    "Book updated"
                );
            }
        } catch (Exception e) {
            logger.error("Failed to publish book updated event for book ID: {}", book.getId(), e);
            // Don't fail the operation if event publishing fails
        }
    }

    private void publishBookDeletedEvent(Book book) {
        try {
            BookDeletedEvent event = new BookDeletedEvent(
                book.getId(),
                book.getIsbn(),
                book.getTitle(),
                null // correlationId will be set by publisher
            );

            eventPublisher.publishBookDeleted(event);
        } catch (Exception e) {
            logger.error("Failed to publish book deleted event for book ID: {}", book.getId(), e);
            // Don't fail the operation if event publishing fails
        }
    }

    private void publishBookAvailabilityChangedEvent(Book book, Integer previousAvailableCopies, 
                                                     String changeReason) {
        try {
            BookAvailabilityChangedEvent event = new BookAvailabilityChangedEvent(
                book.getId(),
                book.getIsbn(),
                book.getTitle(),
                previousAvailableCopies,
                book.getAvailableCopies(),
                book.getTotalCopies(),
                changeReason,
                null // correlationId will be set by publisher
            );

            eventPublisher.publishBookAvailabilityChanged(event);
        } catch (Exception e) {
            logger.error("Failed to publish book availability changed event for book ID: {}", 
                        book.getId(), e);
            // Don't fail the operation if event publishing fails
        }
    }
}
