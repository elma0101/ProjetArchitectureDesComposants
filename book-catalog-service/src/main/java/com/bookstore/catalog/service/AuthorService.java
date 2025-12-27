package com.bookstore.catalog.service;

import com.bookstore.catalog.dto.AuthorRequest;
import com.bookstore.catalog.dto.AuthorResponse;
import com.bookstore.catalog.entity.Author;
import com.bookstore.catalog.exception.AuthorNotFoundException;
import com.bookstore.catalog.repository.AuthorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing authors in the catalog
 */
@Service
@Transactional
public class AuthorService {

    private static final Logger logger = LoggerFactory.getLogger(AuthorService.class);

    private final AuthorRepository authorRepository;

    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    /**
     * Create a new author
     */
    public AuthorResponse createAuthor(AuthorRequest request) {
        logger.info("Creating author: {} {}", request.getFirstName(), request.getLastName());

        Author author = mapToEntity(request);
        Author savedAuthor = authorRepository.save(author);

        logger.info("Author created successfully with ID: {}", savedAuthor.getId());
        return mapToResponse(savedAuthor);
    }

    /**
     * Get author by ID
     */
    @Transactional(readOnly = true)
    public AuthorResponse getAuthorById(Long id) {
        logger.debug("Fetching author with ID: {}", id);
        Author author = authorRepository.findById(id)
            .orElseThrow(() -> new AuthorNotFoundException(id));
        return mapToResponse(author);
    }

    /**
     * Get all authors
     */
    @Transactional(readOnly = true)
    public List<AuthorResponse> getAllAuthors() {
        logger.debug("Fetching all authors");
        return authorRepository.findAll().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Update author
     */
    public AuthorResponse updateAuthor(Long id, AuthorRequest request) {
        logger.info("Updating author with ID: {}", id);

        Author author = authorRepository.findById(id)
            .orElseThrow(() -> new AuthorNotFoundException(id));

        author.setFirstName(request.getFirstName());
        author.setLastName(request.getLastName());
        author.setBiography(request.getBiography());
        author.setBirthDate(request.getBirthDate());
        author.setNationality(request.getNationality());

        Author updatedAuthor = authorRepository.save(author);
        logger.info("Author updated successfully with ID: {}", updatedAuthor.getId());

        return mapToResponse(updatedAuthor);
    }

    /**
     * Delete author
     */
    public void deleteAuthor(Long id) {
        logger.info("Deleting author with ID: {}", id);

        Author author = authorRepository.findById(id)
            .orElseThrow(() -> new AuthorNotFoundException(id));

        authorRepository.delete(author);
        logger.info("Author deleted successfully with ID: {}", id);
    }

    /**
     * Search authors by keyword
     */
    @Transactional(readOnly = true)
    public List<AuthorResponse> searchAuthors(String keyword) {
        logger.debug("Searching authors with keyword: {}", keyword);
        return authorRepository.searchAuthors(keyword).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Find authors by nationality
     */
    @Transactional(readOnly = true)
    public List<AuthorResponse> findAuthorsByNationality(String nationality) {
        logger.debug("Finding authors by nationality: {}", nationality);
        return authorRepository.findByNationality(nationality).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Find authors by book
     */
    @Transactional(readOnly = true)
    public List<AuthorResponse> findAuthorsByBook(Long bookId) {
        logger.debug("Finding authors by book ID: {}", bookId);
        return authorRepository.findByBookId(bookId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    // Mapping methods

    private Author mapToEntity(AuthorRequest request) {
        Author author = new Author();
        author.setFirstName(request.getFirstName());
        author.setLastName(request.getLastName());
        author.setBiography(request.getBiography());
        author.setBirthDate(request.getBirthDate());
        author.setNationality(request.getNationality());
        return author;
    }

    private AuthorResponse mapToResponse(Author author) {
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
}
