package com.bookstore.catalog.repository;

import com.bookstore.catalog.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    List<Book> findByTitleContainingIgnoreCase(String title);

    List<Book> findByGenre(String genre);

    List<Book> findByGenreIgnoreCase(String genre);

    @Query("SELECT b FROM Book b JOIN b.authors a WHERE a.id = :authorId")
    List<Book> findByAuthorId(@Param("authorId") Long authorId);

    @Query("SELECT b FROM Book b WHERE b.availableCopies > 0")
    List<Book> findAvailableBooks();

    @Query("SELECT b FROM Book b WHERE b.availableCopies = 0 AND b.totalCopies > 0")
    List<Book> findUnavailableBooks();

    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Book> searchBooks(@Param("keyword") String keyword);

    @Query("SELECT b FROM Book b WHERE b.publicationYear = :year")
    List<Book> findByPublicationYear(@Param("year") Integer year);

    @Query("SELECT b FROM Book b WHERE b.publicationYear BETWEEN :startYear AND :endYear")
    List<Book> findByPublicationYearBetween(@Param("startYear") Integer startYear, @Param("endYear") Integer endYear);

    boolean existsByIsbn(String isbn);

    @Query("SELECT COUNT(b) FROM Book b WHERE b.availableCopies > 0")
    long countAvailableBooks();

    @Query("SELECT DISTINCT b.genre FROM Book b WHERE b.genre IS NOT NULL ORDER BY b.genre")
    List<String> findAllGenres();
}

