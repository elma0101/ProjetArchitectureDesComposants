package com.bookstore.catalog.repository;

import com.bookstore.catalog.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    List<Author> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);

    List<Author> findByNationality(String nationality);

    @Query("SELECT a FROM Author a WHERE LOWER(a.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(a.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(a.biography) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Author> searchAuthors(@Param("keyword") String keyword);

    @Query("SELECT a FROM Author a JOIN a.books b WHERE b.id = :bookId")
    List<Author> findByBookId(@Param("bookId") Long bookId);

    @Query("SELECT a FROM Author a WHERE LOWER(CONCAT(a.firstName, ' ', a.lastName)) = LOWER(:fullName)")
    List<Author> findByFullName(@Param("fullName") String fullName);
}

