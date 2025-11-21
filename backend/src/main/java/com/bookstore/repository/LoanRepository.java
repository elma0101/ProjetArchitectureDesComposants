package com.bookstore.repository;

import com.bookstore.entity.Loan;
import com.bookstore.entity.LoanStatus;
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
@RepositoryRestResource(collectionResourceRel = "loans", path = "loans")
public interface LoanRepository extends JpaRepository<Loan, Long> {
    
    // Find by loan status
    Page<Loan> findByStatus(LoanStatus status, Pageable pageable);
    
    // Find active loans
    Page<Loan> findByStatusOrderByDueDateAsc(LoanStatus status, Pageable pageable);
    
    // Find overdue loans
    @Query("SELECT l FROM Loan l WHERE l.status = 'ACTIVE' AND l.dueDate < CURRENT_DATE")
    Page<Loan> findOverdueLoans(Pageable pageable);
    
    // Find loans by borrower email
    Page<Loan> findByBorrowerEmailIgnoreCase(String borrowerEmail, Pageable pageable);
    
    // Find loans by borrower ID
    Page<Loan> findByBorrowerId(String borrowerId, Pageable pageable);
    
    // Find loans by borrower name (case-insensitive)
    Page<Loan> findByBorrowerNameContainingIgnoreCase(String borrowerName, Pageable pageable);
    
    // Find loans by book ID
    Page<Loan> findByBookId(Long bookId, Pageable pageable);
    
    // Find loans by book title (case-insensitive)
    @Query("SELECT l FROM Loan l WHERE LOWER(l.book.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<Loan> findByBookTitleContainingIgnoreCase(@Param("title") String title, Pageable pageable);
    
    // Find loans by date range
    Page<Loan> findByLoanDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    // Find loans due in a specific date range
    Page<Loan> findByDueDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    // Find loans due today
    @Query("SELECT l FROM Loan l WHERE l.dueDate = CURRENT_DATE AND l.status = 'ACTIVE'")
    List<Loan> findLoansDueToday();
    
    // Find loans due within specified days
    @Query("SELECT l FROM Loan l WHERE l.dueDate BETWEEN CURRENT_DATE AND :endDate AND l.status = 'ACTIVE'")
    List<Loan> findLoansDueWithinDays(@Param("endDate") LocalDate endDate);
    
    // Check if book is currently loaned
    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM Loan l WHERE l.book.id = :bookId AND l.status = 'ACTIVE'")
    boolean isBookCurrentlyLoaned(@Param("bookId") Long bookId);
    
    // Find active loan for a specific book
    @Query("SELECT l FROM Loan l WHERE l.book.id = :bookId AND l.status = 'ACTIVE'")
    List<Loan> findActiveLoansByBookId(@Param("bookId") Long bookId);
    
    // Count active loans by borrower
    @Query("SELECT COUNT(l) FROM Loan l WHERE l.borrowerEmail = :borrowerEmail AND l.status = 'ACTIVE'")
    Long countActiveLoansByBorrower(@Param("borrowerEmail") String borrowerEmail);
    
    // Find loan history for a borrower
    @Query("SELECT l FROM Loan l WHERE l.borrowerEmail = :borrowerEmail ORDER BY l.loanDate DESC")
    Page<Loan> findLoanHistoryByBorrower(@Param("borrowerEmail") String borrowerEmail, Pageable pageable);
    
    // Find most borrowed books
    @Query("SELECT l.book.id, l.book.title, COUNT(l) as loanCount FROM Loan l " +
           "GROUP BY l.book.id, l.book.title ORDER BY COUNT(l) DESC")
    Page<Object[]> findMostBorrowedBooks(Pageable pageable);
    
    // Find borrowers with most loans
    @Query("SELECT l.borrowerEmail, l.borrowerName, COUNT(l) as loanCount FROM Loan l " +
           "GROUP BY l.borrowerEmail, l.borrowerName ORDER BY COUNT(l) DESC")
    Page<Object[]> findMostActiveBorrowers(Pageable pageable);
    
    // Search loans by multiple criteria
    @Query("SELECT l FROM Loan l WHERE " +
           "(:borrowerEmail IS NULL OR LOWER(l.borrowerEmail) LIKE LOWER(CONCAT('%', :borrowerEmail, '%'))) AND " +
           "(:borrowerName IS NULL OR LOWER(l.borrowerName) LIKE LOWER(CONCAT('%', :borrowerName, '%'))) AND " +
           "(:status IS NULL OR l.status = :status) AND " +
           "(:startDate IS NULL OR l.loanDate >= :startDate) AND " +
           "(:endDate IS NULL OR l.loanDate <= :endDate)")
    Page<Loan> searchLoans(@Param("borrowerEmail") String borrowerEmail,
                          @Param("borrowerName") String borrowerName,
                          @Param("status") LoanStatus status,
                          @Param("startDate") LocalDate startDate,
                          @Param("endDate") LocalDate endDate,
                          Pageable pageable);
    
    // Find loans returned late
    @Query("SELECT l FROM Loan l WHERE l.status = 'RETURNED' AND l.returnDate > l.dueDate")
    Page<Loan> findLateReturns(Pageable pageable);
    
    // Count loans by status
    Long countByStatus(LoanStatus status);
    
    // Find recent loans
    @Query("SELECT l FROM Loan l ORDER BY l.createdAt DESC")
    Page<Loan> findRecentLoans(Pageable pageable);
    
    // Count loans by book and status
    Long countByBookAndStatus(com.bookstore.entity.Book book, LoanStatus status);
    
    // Check if borrower has active loan for specific book
    boolean existsByBookAndBorrowerEmailAndStatus(com.bookstore.entity.Book book, String borrowerEmail, LoanStatus status);
}