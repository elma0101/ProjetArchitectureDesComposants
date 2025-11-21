package com.bookstore.service;

import com.bookstore.dto.*;
import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import com.bookstore.exception.BookNotFoundException;
import com.bookstore.exception.DuplicateResourceException;
import com.bookstore.repository.AuthorRepository;
import com.bookstore.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdvancedBookService {
    
    private static final Logger logger = LoggerFactory.getLogger(AdvancedBookService.class);
    private static final String UPLOAD_DIR = "uploads/book-images/";
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private AuthorRepository authorRepository;
    
    /**
     * Bulk create books
     */
    @CacheEvict(value = {"books", "bookStats", "bookSearch"}, allEntries = true)
    public BulkOperationResult<Book> bulkCreateBooks(BulkBookRequest request) {
        logger.info("Starting bulk creation of {} books", request.getBooks().size());
        
        BulkOperationResult<Book> result = new BulkOperationResult<>();
        
        for (int i = 0; i < request.getBooks().size(); i++) {
            BookCreateRequest bookRequest = request.getBooks().get(i);
            try {
                // Check for duplicate ISBN
                if (bookRepository.existsByIsbn(bookRequest.getIsbn())) {
                    result.addError(new BulkOperationResult.BulkOperationError(
                        i, "Book with ISBN " + bookRequest.getIsbn() + " already exists", bookRequest));
                    continue;
                }
                
                Book book = createBookFromRequest(bookRequest);
                Book savedBook = bookRepository.save(book);
                result.addSuccessfulItem(savedBook);
                
                logger.debug("Successfully created book: {}", savedBook.getTitle());
                
            } catch (Exception e) {
                logger.error("Failed to create book at index {}: {}", i, e.getMessage());
                result.addError(new BulkOperationResult.BulkOperationError(
                    i, e.getMessage(), bookRequest));
            }
        }
        
        logger.info("Bulk creation completed. Success: {}, Failures: {}", 
                   result.getSuccessCount(), result.getFailureCount());
        
        return result;
    }
    
    /**
     * Bulk update books
     */
    @CacheEvict(value = {"books", "bookStats", "bookSearch"}, allEntries = true)
    public BulkOperationResult<Book> bulkUpdateBooks(List<BookUpdateRequest> requests) {
        logger.info("Starting bulk update of {} books", requests.size());
        
        BulkOperationResult<Book> result = new BulkOperationResult<>();
        
        for (int i = 0; i < requests.size(); i++) {
            BookUpdateRequest updateRequest = requests.get(i);
            try {
                Optional<Book> bookOpt = bookRepository.findById(updateRequest.getId());
                if (bookOpt.isEmpty()) {
                    result.addError(new BulkOperationResult.BulkOperationError(
                        i, "Book with ID " + updateRequest.getId() + " not found", updateRequest));
                    continue;
                }
                
                Book book = bookOpt.get();
                updateBookFromRequest(book, updateRequest);
                Book savedBook = bookRepository.save(book);
                result.addSuccessfulItem(savedBook);
                
                logger.debug("Successfully updated book: {}", savedBook.getTitle());
                
            } catch (Exception e) {
                logger.error("Failed to update book at index {}: {}", i, e.getMessage());
                result.addError(new BulkOperationResult.BulkOperationError(
                    i, e.getMessage(), updateRequest));
            }
        }
        
        logger.info("Bulk update completed. Success: {}, Failures: {}", 
                   result.getSuccessCount(), result.getFailureCount());
        
        return result;
    }
    
    /**
     * Get book availability status and notifications
     */
    @Cacheable(value = "books", key = "'availability:' + #bookId")
    public BookAvailabilityStatus getBookAvailabilityStatus(Long bookId) {
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + bookId));
        
        BookAvailabilityStatus status = new BookAvailabilityStatus();
        status.setBookId(bookId);
        status.setTitle(book.getTitle());
        status.setAvailableCopies(book.getAvailableCopies());
        status.setTotalCopies(book.getTotalCopies());
        status.setIsAvailable(book.isAvailable());
        
        // Calculate availability percentage
        if (book.getTotalCopies() > 0) {
            double percentage = (double) book.getAvailableCopies() / book.getTotalCopies() * 100;
            status.setAvailabilityPercentage(percentage);
        }
        
        // Generate notifications
        List<String> notifications = new ArrayList<>();
        if (book.getAvailableCopies() == 0) {
            notifications.add("Book is currently out of stock");
        } else if (book.getAvailableCopies() <= 2) {
            notifications.add("Low stock warning: Only " + book.getAvailableCopies() + " copies remaining");
        }
        
        status.setNotifications(notifications);
        status.setLastUpdated(LocalDateTime.now());
        
        return status;
    }
    
    /**
     * Get books with low stock
     */
    public List<BookAvailabilityStatus> getBooksWithLowStock(int threshold) {
        List<Book> lowStockBooks = bookRepository.findBooksWithLowStock(threshold);
        
        return lowStockBooks.stream()
            .map(book -> {
                BookAvailabilityStatus status = new BookAvailabilityStatus();
                status.setBookId(book.getId());
                status.setTitle(book.getTitle());
                status.setAvailableCopies(book.getAvailableCopies());
                status.setTotalCopies(book.getTotalCopies());
                status.setIsAvailable(book.isAvailable());
                
                if (book.getTotalCopies() > 0) {
                    double percentage = (double) book.getAvailableCopies() / book.getTotalCopies() * 100;
                    status.setAvailabilityPercentage(percentage);
                }
                
                List<String> notifications = new ArrayList<>();
                notifications.add("Low stock: " + book.getAvailableCopies() + " copies remaining");
                status.setNotifications(notifications);
                status.setLastUpdated(LocalDateTime.now());
                
                return status;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Get book statistics
     */
    @Cacheable(value = "bookStats", key = "'all'")
    public BookStatistics getBookStatistics() {
        BookStatistics stats = new BookStatistics();
        
        long totalBooks = bookRepository.count();
        List<Book> allBooks = bookRepository.findAll();
        
        stats.setTotalBooks(totalBooks);
        stats.setTotalCopies(allBooks.stream().mapToInt(b -> b.getTotalCopies() != null ? b.getTotalCopies() : 0).sum());
        stats.setAvailableCopies(allBooks.stream().mapToInt(b -> b.getAvailableCopies() != null ? b.getAvailableCopies() : 0).sum());
        stats.setOutOfStockBooks(bookRepository.findOutOfStockBooks().size());
        
        // Genre distribution
        Map<String, Long> genreDistribution = allBooks.stream()
            .filter(book -> book.getGenre() != null && !book.getGenre().trim().isEmpty())
            .collect(Collectors.groupingBy(
                book -> book.getGenre().toLowerCase(),
                Collectors.counting()
            ));
        stats.setGenreDistribution(genreDistribution);
        
        // Publication year distribution
        Map<Integer, Long> yearDistribution = allBooks.stream()
            .filter(book -> book.getPublicationYear() != null)
            .collect(Collectors.groupingBy(
                Book::getPublicationYear,
                Collectors.counting()
            ));
        stats.setPublicationYearDistribution(yearDistribution);
        
        stats.setGeneratedAt(LocalDateTime.now());
        
        return stats;
    }
    
    /**
     * Upload book image
     */
    public String uploadBookImage(Long bookId, MultipartFile file) throws IOException {
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + bookId));
        
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }
        
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf("."))
            : ".jpg";
        String filename = "book_" + bookId + "_" + System.currentTimeMillis() + extension;
        
        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Update book with image URL
        String imageUrl = "/api/books/" + bookId + "/image/" + filename;
        book.setImageUrl(imageUrl);
        bookRepository.save(book);
        
        logger.info("Uploaded image for book {}: {}", bookId, filename);
        
        return imageUrl;
    }
    
    /**
     * Delete book image
     */
    public void deleteBookImage(Long bookId) throws IOException {
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + bookId));
        
        if (book.getImageUrl() != null) {
            // Extract filename from URL
            String imageUrl = book.getImageUrl();
            String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            
            // Delete file
            Path filePath = Paths.get(UPLOAD_DIR, filename);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                logger.info("Deleted image file: {}", filename);
            }
            
            // Update book
            book.setImageUrl(null);
            bookRepository.save(book);
        }
    }
    
    private Book createBookFromRequest(BookCreateRequest request) {
        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setIsbn(request.getIsbn());
        book.setDescription(request.getDescription());
        book.setPublicationYear(request.getPublicationYear());
        book.setGenre(request.getGenre());
        book.setAvailableCopies(request.getAvailableCopies());
        book.setTotalCopies(request.getTotalCopies());
        book.setImageUrl(request.getImageUrl());
        
        // Add authors if provided
        if (request.getAuthorIds() != null && !request.getAuthorIds().isEmpty()) {
            Set<Author> authors = new HashSet<>();
            for (Long authorId : request.getAuthorIds()) {
                authorRepository.findById(authorId).ifPresent(authors::add);
            }
            book.setAuthors(authors);
        }
        
        return book;
    }
    
    private void updateBookFromRequest(Book book, BookUpdateRequest request) {
        if (request.getTitle() != null) {
            book.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            book.setDescription(request.getDescription());
        }
        if (request.getPublicationYear() != null) {
            book.setPublicationYear(request.getPublicationYear());
        }
        if (request.getGenre() != null) {
            book.setGenre(request.getGenre());
        }
        if (request.getAvailableCopies() != null) {
            book.setAvailableCopies(request.getAvailableCopies());
        }
        if (request.getTotalCopies() != null) {
            book.setTotalCopies(request.getTotalCopies());
        }
        if (request.getImageUrl() != null) {
            book.setImageUrl(request.getImageUrl());
        }
        
        // Update authors if provided
        if (request.getAuthorIds() != null) {
            Set<Author> authors = new HashSet<>();
            for (Long authorId : request.getAuthorIds()) {
                authorRepository.findById(authorId).ifPresent(authors::add);
            }
            book.setAuthors(authors);
        }
    }
}