package com.bookstore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Schema(description = "Request object for updating an existing author")
public class AuthorUpdateRequest {
    
    @Size(max = 100, message = "First name must not exceed 100 characters")
    @Schema(description = "Author's first name", example = "Robert")
    private String firstName;
    
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    @Schema(description = "Author's last name", example = "Martin")
    private String lastName;
    
    @Size(max = 1000, message = "Biography must not exceed 1000 characters")
    @Schema(description = "Author's biography", example = "Robert C. Martin is a software engineer and author known for his work on software design principles.")
    private String biography;
    
    @Past(message = "Birth date must be in the past")
    @Schema(description = "Author's birth date", example = "1952-12-05")
    private LocalDate birthDate;
    
    @Size(max = 100, message = "Nationality must not exceed 100 characters")
    @Schema(description = "Author's nationality", example = "American")
    private String nationality;
    
    // Default constructor
    public AuthorUpdateRequest() {}
    
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
    
    public String getBiography() {
        return biography;
    }
    
    public void setBiography(String biography) {
        this.biography = biography;
    }
    
    public LocalDate getBirthDate() {
        return birthDate;
    }
    
    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
    
    public String getNationality() {
        return nationality;
    }
    
    public void setNationality(String nationality) {
        this.nationality = nationality;
    }
    
    @Override
    public String toString() {
        return "AuthorUpdateRequest{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", nationality='" + nationality + '\'' +
                '}';
    }
}