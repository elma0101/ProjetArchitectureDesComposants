package com.bookstore.loanmanagement.repository;

import com.bookstore.loanmanagement.entity.Loan;
import com.bookstore.loanmanagement.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByUserId(Long userId);

    List<Loan> findByBookId(Long bookId);

    List<Loan> findByStatus(LoanStatus status);

    List<Loan> findByUserIdAndStatus(Long userId, LoanStatus status);

    List<Loan> findByBookIdAndStatus(Long bookId, LoanStatus status);

    @Query("SELECT l FROM Loan l WHERE l.status = 'ACTIVE' AND l.dueDate < :date")
    List<Loan> findOverdueLoans(@Param("date") LocalDate date);

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.userId = :userId AND l.status = 'ACTIVE'")
    long countActiveLoansForUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.bookId = :bookId AND l.status = 'ACTIVE'")
    long countActiveLoansForBook(@Param("bookId") Long bookId);
}
