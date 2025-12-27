package com.bookstore.catalog.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for bulk operation results
 */
public class BulkOperationResult {

    private int totalRequested;
    private int successCount;
    private int failureCount;
    private List<String> errors;
    private List<Long> successfulIds;

    // Constructors
    public BulkOperationResult() {
        this.errors = new ArrayList<>();
        this.successfulIds = new ArrayList<>();
    }

    public BulkOperationResult(int totalRequested) {
        this();
        this.totalRequested = totalRequested;
    }

    // Getters and Setters
    public int getTotalRequested() {
        return totalRequested;
    }

    public void setTotalRequested(int totalRequested) {
        this.totalRequested = totalRequested;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public List<Long> getSuccessfulIds() {
        return successfulIds;
    }

    public void setSuccessfulIds(List<Long> successfulIds) {
        this.successfulIds = successfulIds;
    }

    // Helper methods
    public void addError(String error) {
        this.errors.add(error);
        this.failureCount++;
    }

    public void addSuccess(Long id) {
        this.successfulIds.add(id);
        this.successCount++;
    }
}
