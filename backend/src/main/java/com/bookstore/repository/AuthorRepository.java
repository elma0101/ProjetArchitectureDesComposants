package com.bookstore.repository;

import com.bookstore.entity.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RepositoryRestResource(collectionResourceRel = "authors", path = "authors")
public interface AuthorRepository extends JpaRepository<Author, Long> {
    
    // Find by first name (case-insensitive)
    Page<Author> findByFirstNameContainingIgnoreCase(String firstName, Pageable pageable);
    
    // Find by last name (case-insensitive)
    Page<Author> findByLastNameContainingIgnoreCase(String lastName, Pageable pageable);
    
    // Find by full name (case-insensitive)
    @Query("SELECT a FROM Author a WHERE " +
           "LOWER(CONCAT(a.firstName, ' ', a.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Author> findByFullNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
    
    // Find by nationality (case-insensitive)
    Page<Author> findByNationalityContainingIgnoreCase(String nationality, Pageable pageable);
    
    // Find by birth year
    @Query("SELECT a FROM Author a WHERE YEAR(a.birthDate) = :year")
    Page<Author> findByBirthYear(@Param("year") Integer year, Pageable pageable);
    
    // Find by birth date range
    Page<Author> findByBirthDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    // Find authors with books
    @Query("SELECT DISTINCT a FROM Author a WHERE SIZE(a.books) > 0")
    Page<Author> findAuthorsWithBooks(Pageable pageable);
    
    // Find authors without books
    @Query("SELECT a FROM Author a WHERE SIZE(a.books) = 0")
    Page<Author> findAuthorsWithoutBooks(Pageable pageable);
    
    // Find authors by book genre
    @Query("SELECT DISTINCT a FROM Author a JOIN a.books b WHERE LOWER(b.genre) = LOWER(:genre)")
    Page<Author> findByBookGenre(@Param("genre") String genre, Pageable pageable);
    
    // Find most prolific authors (authors with most books)
    @Query("SELECT a FROM Author a LEFT JOIN a.books b GROUP BY a ORDER BY COUNT(b) DESC")
    Page<Author> findMostProlificAuthors(Pageable pageable);
    
    // Search authors by multiple criteria
    @Query("SELECT a FROM Author a WHERE " +
           "(:firstName IS NULL OR LOWER(a.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) AND " +
           "(:lastName IS NULL OR LOWER(a.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) AND " +
           "(:nationality IS NULL OR LOWER(a.nationality) LIKE LOWER(CONCAT('%', :nationality, '%'))) AND " +
           "(:birthYear IS NULL OR YEAR(a.birthDate) = :birthYear)")
    Page<Author> searchAuthors(@Param("firstName") String firstName,
                              @Param("lastName") String lastName,
                              @Param("nationality") String nationality,
                              @Param("birthYear") Integer birthYear,
                              Pageable pageable);
    
    // Find authors born in a specific decade
    @Query("SELECT a FROM Author a WHERE YEAR(a.birthDate) BETWEEN :startYear AND :endYear")
    List<Author> findAuthorsBornInDecade(@Param("startYear") Integer startYear, @Param("endYear") Integer endYear);
    
    // Count authors by nationality
    @Query("SELECT COUNT(a) FROM Author a WHERE LOWER(a.nationality) = LOWER(:nationality)")
    Long countByNationality(@Param("nationality") String nationality);
    
    // Find recently added authors
    @Query("SELECT a FROM Author a ORDER BY a.createdAt DESC")
    Page<Author> findRecentlyAddedAuthors(Pageable pageable);
    
    // Check if author exists by full name
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Author a WHERE " +
           "LOWER(a.firstName) = LOWER(:firstName) AND LOWER(a.lastName) = LOWER(:lastName)")
    boolean existsByFullName(@Param("firstName") String firstName, @Param("lastName") String lastName);
    
    // Find authors by exact full name
    @Query("SELECT a FROM Author a WHERE " +
           "LOWER(a.firstName) = LOWER(:firstName) AND LOWER(a.lastName) = LOWER(:lastName)")
    List<Author> findByExactFullName(@Param("firstName") String firstName, @Param("lastName") String lastName);
}