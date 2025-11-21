package com.bookstore.service;

import com.bookstore.dto.*;
import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import com.bookstore.exception.AuthorNotFoundException;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class AuthorService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthorService.class);
    
    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    
    @Autowired
    public AuthorService(AuthorRepository authorRepository, BookRepository bookRepository) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
    }
    
    /**
     * Create a new author
     */
    @CacheEvict(value = {"authors", "authorStats", "authorSearch"}, allEntries = true)
    public Author createAuthor(AuthorCreateRequest request) {
        logger.info("Creating new author: {} {}", request.getFirstName(), request.getLastName());
        
        // Check if author already exists
        if (authorRepository.existsByFullName(request.getFirstName(), request.getLastName())) {
            throw new DuplicateResourceException("Author with name '" + 
                request.getFirstName() + " " + request.getLastName() + "' already exists");
        }
        
        Author author = new Author();
        author.setFirstName(request.getFirstName());
        author.setLastName(request.getLastName());
        author.setBiography(request.getBiography());
        author.setBirthDate(request.getBirthDate());
        author.setNationality(request.getNationality());
        
        Author savedAuthor = authorRepository.save(author);
        logger.info("Successfully created author with ID: {}", savedAuthor.getId());
        
        return savedAuthor;
    }
    
    /**
     * Update an existing author
     */
    @CacheEvict(value = {"authors", "authorStats", "authorSearch"}, allEntries = true)
    public Author updateAuthor(Long authorId, AuthorUpdateRequest request) {
        logger.info("Updating author with ID: {}", authorId);
        
        Author author = authorRepository.findById(authorId)
            .orElseThrow(() -> new AuthorNotFoundException("Author not found with ID: " + authorId));
        
        // Update only non-null fields
        if (request.getFirstName() != null) {
            author.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            author.setLastName(request.getLastName());
        }
        if (request.getBiography() != null) {
            author.setBiography(request.getBiography());
        }
        if (request.getBirthDate() != null) {
            author.setBirthDate(request.getBirthDate());
        }
        if (request.getNationality() != null) {
            author.setNationality(request.getNationality());
        }
        
        Author updatedAuthor = authorRepository.save(author);
        logger.info("Successfully updated author with ID: {}", authorId);
        
        return updatedAuthor;
    }
    
    /**
     * Get author by ID
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "authors", key = "#authorId")
    public Author getAuthorById(Long authorId) {
        return authorRepository.findById(authorId)
            .orElseThrow(() -> new AuthorNotFoundException("Author not found with ID: " + authorId));
    }
    
    /**
     * Get all authors with pagination
     */
    @Transactional(readOnly = true)
    public Page<Author> getAllAuthors(Pageable pageable) {
        return authorRepository.findAll(pageable);
    }
    
    /**
     * Delete an author
     */
    @CacheEvict(value = {"authors", "authorStats", "authorSearch"}, allEntries = true)
    public void deleteAuthor(Long authorId) {
        logger.info("Deleting author with ID: {}", authorId);
        
        Author author = authorRepository.findById(authorId)
            .orElseThrow(() -> new AuthorNotFoundException("Author not found with ID: " + authorId));
        
        // Remove author from all associated books
        for (Book book : author.getBooks()) {
            book.getAuthors().remove(author);
        }
        
        authorRepository.delete(author);
        logger.info("Successfully deleted author with ID: {}", authorId);
    }
    
    /**
     * Add books to an author
     */
    public Author addBooksToAuthor(Long authorId, AuthorBookAssociationRequest request) {
        logger.info("Adding books to author with ID: {}", authorId);
        
        Author author = authorRepository.findById(authorId)
            .orElseThrow(() -> new AuthorNotFoundException("Author not found with ID: " + authorId));
        
        Set<Book> booksToAdd = new HashSet<>();
        for (Long bookId : request.getBookIds()) {
            Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + bookId));
            booksToAdd.add(book);
        }
        
        // Add books to author
        for (Book book : booksToAdd) {
            author.addBook(book);
        }
        
        Author updatedAuthor = authorRepository.save(author);
        logger.info("Successfully added {} books to author with ID: {}", booksToAdd.size(), authorId);
        
        return updatedAuthor;
    }
    
    /**
     * Remove books from an author
     */
    public Author removeBooksFromAuthor(Long authorId, AuthorBookAssociationRequest request) {
        logger.info("Removing books from author with ID: {}", authorId);
        
        Author author = authorRepository.findById(authorId)
            .orElseThrow(() -> new AuthorNotFoundException("Author not found with ID: " + authorId));
        
        Set<Book> booksToRemove = new HashSet<>();
        for (Long bookId : request.getBookIds()) {
            Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + bookId));
            booksToRemove.add(book);
        }
        
        // Remove books from author
        for (Book book : booksToRemove) {
            author.removeBook(book);
        }
        
        Author updatedAuthor = authorRepository.save(author);
        logger.info("Successfully removed {} books from author with ID: {}", booksToRemove.size(), authorId);
        
        return updatedAuthor;
    }
    
    /**
     * Set books for an author (replace all existing associations)
     */
    public Author setBooksForAuthor(Long authorId, AuthorBookAssociationRequest request) {
        logger.info("Setting books for author with ID: {}", authorId);
        
        Author author = authorRepository.findById(authorId)
            .orElseThrow(() -> new AuthorNotFoundException("Author not found with ID: " + authorId));
        
        // Remove all existing book associations
        Set<Book> currentBooks = new HashSet<>(author.getBooks());
        for (Book book : currentBooks) {
            author.removeBook(book);
        }
        
        // Add new book associations
        Set<Book> newBooks = new HashSet<>();
        for (Long bookId : request.getBookIds()) {
            Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + bookId));
            newBooks.add(book);
        }
        
        for (Book book : newBooks) {
            author.addBook(book);
        }
        
        Author updatedAuthor = authorRepository.save(author);
        logger.info("Successfully set {} books for author with ID: {}", newBooks.size(), authorId);
        
        return updatedAuthor;
    }
    
    /**
     * Get books by author
     */
    @Transactional(readOnly = true)
    public Set<Book> getBooksByAuthor(Long authorId) {
        Author author = authorRepository.findById(authorId)
            .orElseThrow(() -> new AuthorNotFoundException("Author not found with ID: " + authorId));
        
        return author.getBooks();
    }
    
    /**
     * Search authors with multiple criteria
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "authorSearch", key = "#request.toString() + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<Author> searchAuthors(AuthorSearchRequest request, Pageable pageable) {
        logger.info("Searching authors with criteria: {}", request);
        
        // If full name is provided, use it for search
        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            return authorRepository.findByFullNameContainingIgnoreCase(request.getFullName().trim(), pageable);
        }
        
        // If genre is provided, search by book genre
        if (request.getGenre() != null && !request.getGenre().trim().isEmpty()) {
            return authorRepository.findByBookGenre(request.getGenre().trim(), pageable);
        }
        
        // If hasBooks filter is provided
        if (request.getHasBooks() != null) {
            if (request.getHasBooks()) {
                return authorRepository.findAuthorsWithBooks(pageable);
            } else {
                return authorRepository.findAuthorsWithoutBooks(pageable);
            }
        }
        
        // Use multi-criteria search
        return authorRepository.searchAuthors(
            request.getFirstName(),
            request.getLastName(),
            request.getNationality(),
            request.getBirthYear(),
            pageable
        );
    }
    
    /**
     * Get author statistics
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "authorStats", key = "#authorId")
    public AuthorStatistics getAuthorStatistics(Long authorId) {
        Author author = authorRepository.findById(authorId)
            .orElseThrow(() -> new AuthorNotFoundException("Author not found with ID: " + authorId));
        
        AuthorStatistics stats = new AuthorStatistics();
        stats.setAuthorId(authorId);
        stats.setFullName(author.getFullName());
        stats.setTotalBooks((long) author.getBooks().size());
        
        // Calculate total and available copies
        long totalCopies = author.getBooks().stream()
            .mapToLong(book -> book.getTotalCopies() != null ? book.getTotalCopies() : 0)
            .sum();
        
        long availableCopies = author.getBooks().stream()
            .mapToLong(book -> book.getAvailableCopies() != null ? book.getAvailableCopies() : 0)
            .sum();
        
        stats.setTotalCopies(totalCopies);
        stats.setAvailableCopies(availableCopies);
        
        // Calculate loan statistics
        long totalLoans = author.getBooks().stream()
            .mapToLong(book -> book.getLoans().size())
            .sum();
        
        long activeLoans = author.getBooks().stream()
            .mapToLong(book -> book.getLoans().stream()
                .mapToLong(loan -> loan.getReturnDate() == null ? 1 : 0)
                .sum())
            .sum();
        
        stats.setTotalLoans(totalLoans);
        stats.setActiveLoans(activeLoans);
        
        return stats;
    }
    
    /**
     * Get most prolific authors
     */
    @Transactional(readOnly = true)
    public Page<Author> getMostProlificAuthors(Pageable pageable) {
        return authorRepository.findMostProlificAuthors(pageable);
    }
    
    /**
     * Get recently added authors
     */
    @Transactional(readOnly = true)
    public Page<Author> getRecentlyAddedAuthors(Pageable pageable) {
        return authorRepository.findRecentlyAddedAuthors(pageable);
    }
    
    /**
     * Find authors by nationality
     */
    @Transactional(readOnly = true)
    public Page<Author> findAuthorsByNationality(String nationality, Pageable pageable) {
        return authorRepository.findByNationalityContainingIgnoreCase(nationality, pageable);
    }
    
    /**
     * Find authors by birth year
     */
    @Transactional(readOnly = true)
    public Page<Author> findAuthorsByBirthYear(Integer year, Pageable pageable) {
        return authorRepository.findByBirthYear(year, pageable);
    }
}