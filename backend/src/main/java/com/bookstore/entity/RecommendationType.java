package com.bookstore.entity;

public enum RecommendationType {
    COLLABORATIVE("Collaborative Filtering"),
    CONTENT_BASED("Content-Based"),
    POPULAR("Popular Books"),
    TRENDING("Trending Books");
    
    private final String displayName;
    
    RecommendationType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}