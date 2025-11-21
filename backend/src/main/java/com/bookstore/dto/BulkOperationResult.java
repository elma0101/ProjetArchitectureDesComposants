package com.bookstore.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "Result of bulk operation")
public class BulkOperationResult<T> {
    
    @Schema(description = "Number of successful operations")
    private int successCount;
    
    @Schema(description = "Number of failed operations")
    private int failureCount;
    
    @Schema(description = "List of successfully processed items")
    private List<T> successfulItems = new ArrayList<>();
    
    @Schema(description = "List of errors for failed operations")
    private List<BulkOperationError> errors = new ArrayList<>();
    
    public BulkOperationResult() {}
    
    public BulkOperationResult(int successCount, int failureCount) {
        this.successCount = successCount;
        this.failureCount = failureCount;
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
    
    public List<T> getSuccessfulItems() {
        return successfulItems;
    }
    
    public void setSuccessfulItems(List<T> successfulItems) {
        this.successfulItems = successfulItems;
    }
    
    public List<BulkOperationError> getErrors() {
        return errors;
    }
    
    public void setErrors(List<BulkOperationError> errors) {
        this.errors = errors;
    }
    
    public void addSuccessfulItem(T item) {
        this.successfulItems.add(item);
        this.successCount++;
    }
    
    public void addError(BulkOperationError error) {
        this.errors.add(error);
        this.failureCount++;
    }
    
    public boolean hasErrors() {
        return failureCount > 0;
    }
    
    @Schema(description = "Error details for failed bulk operations")
    public static class BulkOperationError {
        @Schema(description = "Index of the item that failed")
        private int index;
        
        @Schema(description = "Error message")
        private String message;
        
        @Schema(description = "Item that failed to process")
        private Object item;
        
        public BulkOperationError() {}
        
        public BulkOperationError(int index, String message, Object item) {
            this.index = index;
            this.message = message;
            this.item = item;
        }
        
        public int getIndex() {
            return index;
        }
        
        public void setIndex(int index) {
            this.index = index;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public Object getItem() {
            return item;
        }
        
        public void setItem(Object item) {
            this.item = item;
        }
    }
}