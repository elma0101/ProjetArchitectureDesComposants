package com.bookstore.rest;

import com.bookstore.dto.*;
import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import com.bookstore.repository.AuthorRepository;
import com.bookstore.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@Import(com.bookstore.config.TestSecurityConfig.class)
class AuthorManagementRestIntegrationTest extends BaseRestIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private AuthorRepository authorRepository;
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private Author testAuthor;
    private Book testBook;
    
    @BeforeEach
    void setUp() {
        // Clean up repositories
        authorRepository.deleteAll();
        bookRepository.deleteAll();
        
        // Create test author
        testAuthor = new Author();
        testAuthor.setFirstName("Robert");
        testAuthor.setLastName("Martin");
        testAuthor.setBiography("Software engineer and author");
        testAuthor.setBirthDate(LocalDate.of(1952, 12, 5));
        testAuthor.setNationality("American");
        testAuthor = authorRepository.save(testAuthor);
        
        // Create test book
        testBook = new Book();
        testBook.setTitle("Clean Code");
        testBook.setIsbn("978-0132350884");
        testBook.setDescription("A handbook of agile software craftsmanship");
        testBook.setPublicationYear(2008);
        testBook.setGenre("Programming");
        testBook.setTotalCopies(5);
        testBook.setAvailableCopies(3);
        testBook = bookRepository.save(testBook);
    }
    
    @Test
    void createAuthor_Success() throws Exception {
        // Given
        AuthorCreateRequest request = new AuthorCreateRequest();
        request.setFirstName("Martin");
        request.setLastName("Fowler");
        request.setBiography("Software developer and author");
        request.setBirthDate(LocalDate.of(1963, 12, 18));
        request.setNationality("British");
        
        // When & Then
        mockMvc.perform(post("/api/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Martin"))
                .andExpect(jsonPath("$.lastName").value("Fowler"))
                .andExpect(jsonPath("$.biography").value("Software developer and author"))
                .andExpect(jsonPath("$.nationality").value("British"))
                .andExpect(jsonPath("$.id").exists());
    }
    
    @Test
    void createAuthor_DuplicateName_Conflict() throws Exception {
        // Given
        AuthorCreateRequest request = new AuthorCreateRequest();
        request.setFirstName("Robert");
        request.setLastName("Martin");
        request.setBiography("Another biography");
        
        // When & Then
        mockMvc.perform(post("/api/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
    
    @Test
    void updateAuthor_Success() throws Exception {
        // Given
        AuthorUpdateRequest request = new AuthorUpdateRequest();
        request.setBiography("Updated biography for Robert Martin");
        request.setNationality("Canadian");
        
        // When & Then
        mockMvc.perform(put("/api/authors/" + testAuthor.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testAuthor.getId()))
                .andExpect(jsonPath("$.firstName").value("Robert"))
                .andExpect(jsonPath("$.lastName").value("Martin"))
                .andExpect(jsonPath("$.biography").value("Updated biography for Robert Martin"))
                .andExpect(jsonPath("$.nationality").value("Canadian"));
    }
    
    @Test
    void getAuthorById_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/authors/" + testAuthor.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testAuthor.getId()))
                .andExpect(jsonPath("$.firstName").value("Robert"))
                .andExpect(jsonPath("$.lastName").value("Martin"))
                .andExpect(jsonPath("$.biography").value("Software engineer and author"))
                .andExpect(jsonPath("$.nationality").value("American"));
    }
    
    @Test
    void getAuthorById_NotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/authors/999"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void getAllAuthors_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(testAuthor.getId()))
                .andExpect(jsonPath("$.content[0].firstName").value("Robert"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
    
    @Test
    void deleteAuthor_Success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/authors/" + testAuthor.getId()))
                .andExpect(status().isNoContent());
        
        // Verify author is deleted
        mockMvc.perform(get("/api/authors/" + testAuthor.getId()))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void addBooksToAuthor_Success() throws Exception {
        // Given
        AuthorBookAssociationRequest request = new AuthorBookAssociationRequest(Set.of(testBook.getId()));
        
        // When & Then
        mockMvc.perform(post("/api/authors/" + testAuthor.getId() + "/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testAuthor.getId()));
        
        // Verify association was created
        mockMvc.perform(get("/api/authors/" + testAuthor.getId() + "/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(testBook.getId()));
    }
    
    @Test
    void removeBooksFromAuthor_Success() throws Exception {
        // Given - First add the book to the author
        testAuthor.addBook(testBook);
        authorRepository.save(testAuthor);
        
        AuthorBookAssociationRequest request = new AuthorBookAssociationRequest(Set.of(testBook.getId()));
        
        // When & Then
        mockMvc.perform(delete("/api/authors/" + testAuthor.getId() + "/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testAuthor.getId()));
        
        // Verify association was removed
        mockMvc.perform(get("/api/authors/" + testAuthor.getId() + "/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }
    
    @Test
    void setBooksForAuthor_Success() throws Exception {
        // Given - Create another book
        Book anotherBook = new Book();
        anotherBook.setTitle("Clean Architecture");
        anotherBook.setIsbn("978-0134494166");
        anotherBook.setDescription("A craftsman's guide to software structure and design");
        anotherBook.setPublicationYear(2017);
        anotherBook.setGenre("Programming");
        anotherBook.setTotalCopies(3);
        anotherBook.setAvailableCopies(2);
        anotherBook = bookRepository.save(anotherBook);
        
        // Add first book to author
        testAuthor.addBook(testBook);
        authorRepository.save(testAuthor);
        
        // Set only the second book
        AuthorBookAssociationRequest request = new AuthorBookAssociationRequest(Set.of(anotherBook.getId()));
        
        // When & Then
        mockMvc.perform(put("/api/authors/" + testAuthor.getId() + "/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testAuthor.getId()));
        
        // Verify only the second book is associated
        mockMvc.perform(get("/api/authors/" + testAuthor.getId() + "/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(anotherBook.getId()));
    }
    
    @Test
    void getBooksByAuthor_Success() throws Exception {
        // Given - Add book to author
        testAuthor.addBook(testBook);
        authorRepository.save(testAuthor);
        
        // When & Then
        mockMvc.perform(get("/api/authors/" + testAuthor.getId() + "/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(testBook.getId()))
                .andExpect(jsonPath("$[0].title").value("Clean Code"));
    }
    
    @Test
    void searchAuthors_WithFullName_Success() throws Exception {
        // Given
        AuthorSearchRequest request = new AuthorSearchRequest();
        request.setFullName("Robert Martin");
        
        // When & Then
        mockMvc.perform(post("/api/authors/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(testAuthor.getId()));
    }
    
    @Test
    void searchAuthors_WithNationality_Success() throws Exception {
        // Given
        AuthorSearchRequest request = new AuthorSearchRequest();
        request.setNationality("American");
        
        // When & Then
        mockMvc.perform(post("/api/authors/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(testAuthor.getId()));
    }
    
    @Test
    void getAuthorStatistics_Success() throws Exception {
        // Given - Add book to author
        testAuthor.addBook(testBook);
        authorRepository.save(testAuthor);
        
        // When & Then
        mockMvc.perform(get("/api/authors/" + testAuthor.getId() + "/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authorId").value(testAuthor.getId()))
                .andExpect(jsonPath("$.fullName").value("Robert Martin"))
                .andExpect(jsonPath("$.totalBooks").value(1))
                .andExpect(jsonPath("$.totalCopies").value(5))
                .andExpect(jsonPath("$.availableCopies").value(3));
    }
    
    @Test
    void getMostProlificAuthors_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/authors/prolific"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(testAuthor.getId()));
    }
    
    @Test
    void getRecentlyAddedAuthors_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/authors/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(testAuthor.getId()));
    }
    
    @Test
    void findAuthorsByNationality_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/authors/nationality/American"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(testAuthor.getId()));
    }
    
    @Test
    void findAuthorsByBirthYear_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/authors/birth-year/1952"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(testAuthor.getId()));
    }
    
    @Test
    void addBooksToAuthor_BookNotFound_NotFound() throws Exception {
        // Given
        AuthorBookAssociationRequest request = new AuthorBookAssociationRequest(Set.of(999L));
        
        // When & Then
        mockMvc.perform(post("/api/authors/" + testAuthor.getId() + "/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void addBooksToAuthor_AuthorNotFound_NotFound() throws Exception {
        // Given
        AuthorBookAssociationRequest request = new AuthorBookAssociationRequest(Set.of(testBook.getId()));
        
        // When & Then
        mockMvc.perform(post("/api/authors/999/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}