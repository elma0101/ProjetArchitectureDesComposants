package com.bookstore.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "recommendations")
public class Recommendation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    @Size(max = 100, message = "User ID must not exceed 100 characters")
    private String userId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    @NotNull(message = "Book is required")
    private Book book;
    
    @Column(name = "recommendation_score")
    @DecimalMin(value = "0.0", message = "Recommendation score must be non-negative")
    @DecimalMax(value = "1.0", message = "Recommendation score must not exceed 1.0")
    private Double score;
    
    @Column(name = "recommendation_reason")
    @Size(max = 500, message = "Recommendation reason must not exceed 500 characters")
    private String reason;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Recommendation type is required")
    private RecommendationType type;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    // Default constructor
    public Recommendation() {}
    
    // Constructor with required fields
    public Recommendation(String userId, Book book, RecommendationType type, Double score) {
        this.userId = userId;
        this.book = book;
        this.type = type;
        this.score = score;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public Book getBook() {
        return book;
    }
    
    public void setBook(Book book) {
        this.book = book;
    }
    
    public Double getScore() {
        return score;
    }
    
    public void setScore(Double score) {
        this.score = score;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public RecommendationType getType() {
        return type;
    }
    
    public void setType(RecommendationType type) {
        this.type = type;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    // Helper methods
    public boolean isHighScore() {
        return score != null && score >= 0.7;
    }
    
    public boolean isRecentRecommendation() {
        return createdAt != null && createdAt.isAfter(LocalDateTime.now().minusDays(7));
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Recommendation)) return false;
        Recommendation that = (Recommendation) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "Recommendation{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", type=" + type +
                ", score=" + score +
                ", createdAt=" + createdAt +
                '}';
    }
}