package com.bookstore.repository;

import com.bookstore.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RepositoryRestResource(collectionResourceRel = "books", path = "books")
public interface BookRepository extends JpaRepository<Book, Long> {
    
    // Find by title (case-insensitive)
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    
    // Find by ISBN
    Optional<Book> findByIsbn(String isbn);
    
    // Find by genre (case-insensitive)
    Page<Book> findByGenreContainingIgnoreCase(String genre, Pageable pageable);
    
    // Find by exact genre (case-insensitive)
    List<Book> findByGenreIgnoreCase(String genre);
    
    // Find by publication year
    Page<Book> findByPublicationYear(Integer publicationYear, Pageable pageable);
    
    // Find by publication year range
    Page<Book> findByPublicationYearBetween(Integer startYear, Integer endYear, Pageable pageable);
    
    // Find available books (books with available copies > 0)
    @Query("SELECT b FROM Book b WHERE b.availableCopies > 0")
    Page<Book> findAvailableBooks(Pageable pageable);
    
    // Find books by author name (case-insensitive)
    @Query("SELECT DISTINCT b FROM Book b JOIN b.authors a WHERE " +
           "LOWER(CONCAT(a.firstName, ' ', a.lastName)) LIKE LOWER(CONCAT('%', :authorName, '%'))")
    Page<Book> findByAuthorNameContainingIgnoreCase(@Param("authorName") String authorName, Pageable pageable);
    
    // Find books by author ID
    @Query("SELECT b FROM Book b JOIN b.authors a WHERE a.id = :authorId")
    Page<Book> findByAuthorId(@Param("authorId") Long authorId, Pageable pageable);
    
    // Search books by multiple criteria
    @Query("SELECT DISTINCT b FROM Book b LEFT JOIN b.authors a WHERE " +
           "(:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:genre IS NULL OR LOWER(b.genre) LIKE LOWER(CONCAT('%', :genre, '%'))) AND " +
           "(:authorName IS NULL OR LOWER(CONCAT(a.firstName, ' ', a.lastName)) LIKE LOWER(CONCAT('%', :authorName, '%'))) AND " +
           "(:publicationYear IS NULL OR b.publicationYear = :publicationYear) AND " +
           "(:availableOnly = false OR b.availableCopies > 0)")
    Page<Book> searchBooks(@Param("title") String title,
                          @Param("genre") String genre,
                          @Param("authorName") String authorName,
                          @Param("publicationYear") Integer publicationYear,
                          @Param("availableOnly") boolean availableOnly,
                          Pageable pageable);
    
    // Find books with low stock (available copies <= threshold)
    @Query("SELECT b FROM Book b WHERE b.availableCopies <= :threshold AND b.availableCopies > 0")
    List<Book> findBooksWithLowStock(@Param("threshold") Integer threshold);
    
    // Find books that are out of stock
    @Query("SELECT b FROM Book b WHERE b.availableCopies = 0 AND b.totalCopies > 0")
    List<Book> findOutOfStockBooks();
    
    // Count books by genre
    @Query("SELECT COUNT(b) FROM Book b WHERE LOWER(b.genre) = LOWER(:genre)")
    Long countByGenre(@Param("genre") String genre);
    
    // Find most popular books (books with most loans)
    @Query("SELECT b FROM Book b LEFT JOIN b.loans l GROUP BY b ORDER BY COUNT(l) DESC")
    Page<Book> findMostPopularBooks(Pageable pageable);
    
    // Find recently added books
    @Query("SELECT b FROM Book b ORDER BY b.createdAt DESC")
    Page<Book> findRecentlyAddedBooks(Pageable pageable);
    
    // Check if ISBN exists
    boolean existsByIsbn(String isbn);
    
    // Find books by multiple ISBNs
    List<Book> findByIsbnIn(List<String> isbns);
}