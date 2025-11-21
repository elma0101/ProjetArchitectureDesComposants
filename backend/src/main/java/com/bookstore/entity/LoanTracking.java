package com.bookstore.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Entity for tracking loan events and history
 */
@Entity
@Table(name = "loan_tracking")
public class LoanTracking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "loan_id", nullable = false)
    @NotNull(message = "Loan ID is required")
    private Long loanId;
    
    @Column(name = "event_type", nullable = false)
    @NotBlank(message = "Event type is required")
    @Size(max = 50, message = "Event type must not exceed 50 characters")
    private String eventType;
    
    @Column(name = "event_description", length = 500)
    @Size(max = 500, message = "Event description must not exceed 500 characters")
    private String eventDescription;
    
    @Column(name = "event_timestamp", nullable = false)
    @NotNull(message = "Event timestamp is required")
    private LocalDateTime eventTimestamp;
    
    @Column(name = "additional_data", length = 1000)
    @Size(max = 1000, message = "Additional data must not exceed 1000 characters")
    private String additionalData;
    
    // Default constructor
    public LoanTracking() {}
    
    // Constructor with required fields
    public LoanTracking(Long loanId, String eventType, String eventDescription, LocalDateTime eventTimestamp) {
        this.loanId = loanId;
        this.eventType = eventType;
        this.eventDescription = eventDescription;
        this.eventTimestamp = eventTimestamp;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getLoanId() {
        return loanId;
    }
    
    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public String getEventDescription() {
        return eventDescription;
    }
    
    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }
    
    public LocalDateTime getEventTimestamp() {
        return eventTimestamp;
    }
    
    public void setEventTimestamp(LocalDateTime eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }
    
    public String getAdditionalData() {
        return additionalData;
    }
    
    public void setAdditionalData(String additionalData) {
        this.additionalData = additionalData;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LoanTracking)) return false;
        LoanTracking that = (LoanTracking) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "LoanTracking{" +
                "id=" + id +
                ", loanId=" + loanId +
                ", eventType='" + eventType + '\'' +
                ", eventDescription='" + eventDescription + '\'' +
                ", eventTimestamp=" + eventTimestamp +
                '}';
    }
}