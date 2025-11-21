package com.bookstore.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "loans", indexes = {
    @Index(name = "idx_loan_book_id", columnList = "book_id"),
    @Index(name = "idx_loan_borrower_id", columnList = "borrower_id"),
    @Index(name = "idx_loan_borrower_email", columnList = "borrower_email"),
    @Index(name = "idx_loan_status", columnList = "status"),
    @Index(name = "idx_loan_date", columnList = "loan_date"),
    @Index(name = "idx_loan_due_date", columnList = "due_date"),
    @Index(name = "idx_loan_return_date", columnList = "return_date"),
    @Index(name = "idx_loan_created_at", columnList = "created_at")
})
@com.bookstore.validation.ValidDateRange(groups = {com.bookstore.validation.ValidationGroups.Create.class, com.bookstore.validation.ValidationGroups.Update.class})
public class Loan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    @NotNull(message = "Book is required")
    private Book book;
    
    @Column(name = "borrower_name", nullable = false)
    @NotBlank(message = "Borrower name is required")
    @Size(max = 200, message = "Borrower name must not exceed 200 characters")
    private String borrowerName;
    
    @Column(name = "borrower_email", nullable = false)
    @NotBlank(message = "Borrower email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String borrowerEmail;
    
    @Column(name = "borrower_id")
    @Size(max = 50, message = "Borrower ID must not exceed 50 characters")
    private String borrowerId;
    
    @Column(name = "loan_date", nullable = false)
    @NotNull(message = "Loan date is required")
    private LocalDate loanDate;
    
    @Column(name = "due_date", nullable = false)
    @NotNull(message = "Due date is required")
    private LocalDate dueDate;
    
    @Column(name = "return_date")
    private LocalDate returnDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Loan status is required")
    private LoanStatus status = LoanStatus.ACTIVE;
    
    @Column(length = 500)
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Default constructor
    public Loan() {}
    
    // Constructor with required fields
    public Loan(Book book, String borrowerName, String borrowerEmail, LocalDate loanDate, LocalDate dueDate) {
        this.book = book;
        this.borrowerName = borrowerName;
        this.borrowerEmail = borrowerEmail;
        this.loanDate = loanDate;
        this.dueDate = dueDate;
        this.status = LoanStatus.ACTIVE;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Book getBook() {
        return book;
    }
    
    public void setBook(Book book) {
        this.book = book;
    }
    
    public String getBorrowerName() {
        return borrowerName;
    }
    
    public void setBorrowerName(String borrowerName) {
        this.borrowerName = borrowerName;
    }
    
    public String getBorrowerEmail() {
        return borrowerEmail;
    }
    
    public void setBorrowerEmail(String borrowerEmail) {
        this.borrowerEmail = borrowerEmail;
    }
    
    public String getBorrowerId() {
        return borrowerId;
    }
    
    public void setBorrowerId(String borrowerId) {
        this.borrowerId = borrowerId;
    }
    
    public LocalDate getLoanDate() {
        return loanDate;
    }
    
    public void setLoanDate(LocalDate loanDate) {
        this.loanDate = loanDate;
    }
    
    public LocalDate getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    
    public LocalDate getReturnDate() {
        return returnDate;
    }
    
    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }
    
    public LoanStatus getStatus() {
        return status;
    }
    
    public void setStatus(LoanStatus status) {
        this.status = status;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    // Helper methods
    public boolean isOverdue() {
        return status == LoanStatus.ACTIVE && LocalDate.now().isAfter(dueDate);
    }
    
    public boolean isActive() {
        return status == LoanStatus.ACTIVE;
    }
    
    public boolean isReturned() {
        return status == LoanStatus.RETURNED;
    }
    
    public void markAsReturned() {
        this.status = LoanStatus.RETURNED;
        this.returnDate = LocalDate.now();
    }
    
    public void markAsOverdue() {
        if (status == LoanStatus.ACTIVE) {
            this.status = LoanStatus.OVERDUE;
        }
    }
    
    public long getDaysOverdue() {
        if (isOverdue()) {
            return LocalDate.now().toEpochDay() - dueDate.toEpochDay();
        }
        return 0;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Loan)) return false;
        Loan loan = (Loan) o;
        return id != null && id.equals(loan.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "Loan{" +
                "id=" + id +
                ", borrowerName='" + borrowerName + '\'' +
                ", loanDate=" + loanDate +
                ", dueDate=" + dueDate +
                ", status=" + status +
                '}';
    }
}