package com.bookstore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request object for searching authors with multiple criteria")
public class AuthorSearchRequest {
    
    @Size(max = 100, message = "First name must not exceed 100 characters")
    @Schema(description = "Author's first name to search for", example = "Robert")
    private String firstName;
    
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    @Schema(description = "Author's last name to search for", example = "Martin")
    private String lastName;
    
    @Size(max = 200, message = "Full name must not exceed 200 characters")
    @Schema(description = "Author's full name to search for", example = "Robert Martin")
    private String fullName;
    
    @Size(max = 100, message = "Nationality must not exceed 100 characters")
    @Schema(description = "Author's nationality to search for", example = "American")
    private String nationality;
    
    @Schema(description = "Birth year to search for", example = "1952")
    private Integer birthYear;
    
    @Size(max = 100, message = "Genre must not exceed 100 characters")
    @Schema(description = "Book genre to filter authors by", example = "Programming")
    private String genre;
    
    @Schema(description = "Whether to include only authors with books", example = "true")
    private Boolean hasBooks;
    
    // Default constructor
    public AuthorSearchRequest() {}
    
    // Getters and Setters
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getNationality() {
        return nationality;
    }
    
    public void setNationality(String nationality) {
        this.nationality = nationality;
    }
    
    public Integer getBirthYear() {
        return birthYear;
    }
    
    public void setBirthYear(Integer birthYear) {
        this.birthYear = birthYear;
    }
    
    public String getGenre() {
        return genre;
    }
    
    public void setGenre(String genre) {
        this.genre = genre;
    }
    
    public Boolean getHasBooks() {
        return hasBooks;
    }
    
    public void setHasBooks(Boolean hasBooks) {
        this.hasBooks = hasBooks;
    }
    
    @Override
    public String toString() {
        return "AuthorSearchRequest{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", fullName='" + fullName + '\'' +
                ", nationality='" + nationality + '\'' +
                ", birthYear=" + birthYear +
                ", genre='" + genre + '\'' +
                ", hasBooks=" + hasBooks +
                '}';
    }
}