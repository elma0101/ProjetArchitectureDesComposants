package com.bookstore.controller;

import com.bookstore.dto.*;
import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import com.bookstore.exception.AuthorNotFoundException;
import com.bookstore.exception.DuplicateResourceException;
import com.bookstore.service.AuthorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthorController.class)
@Import(com.bookstore.config.TestSecurityConfig.class)
class AuthorControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private AuthorService authorService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private Author testAuthor;
    private Book testBook;
    private AuthorCreateRequest createRequest;
    private AuthorUpdateRequest updateRequest;
    
    @BeforeEach
    void setUp() {
        testAuthor = new Author();
        testAuthor.setId(1L);
        testAuthor.setFirstName("Robert");
        testAuthor.setLastName("Martin");
        testAuthor.setBiography("Software engineer and author");
        testAuthor.setBirthDate(LocalDate.of(1952, 12, 5));
        testAuthor.setNationality("American");
        
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Clean Code");
        testBook.setIsbn("978-0132350884");
        
        createRequest = new AuthorCreateRequest();
        createRequest.setFirstName("Robert");
        createRequest.setLastName("Martin");
        createRequest.setBiography("Software engineer and author");
        createRequest.setBirthDate(LocalDate.of(1952, 12, 5));
        createRequest.setNationality("American");
        
        updateRequest = new AuthorUpdateRequest();
        updateRequest.setBiography("Updated biography");
        updateRequest.setNationality("Canadian");
    }
    
    @Test
    void createAuthor_Success() throws Exception {
        // Given
        when(authorService.createAuthor(any(AuthorCreateRequest.class))).thenReturn(testAuthor);
        
        // When & Then
        mockMvc.perform(post("/api/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("Robert"))
                .andExpect(jsonPath("$.lastName").value("Martin"));
        
        verify(authorService).createAuthor(any(AuthorCreateRequest.class));
    }
    
    @Test
    void createAuthor_InvalidInput_BadRequest() throws Exception {
        // Given
        AuthorCreateRequest invalidRequest = new AuthorCreateRequest();
        // Missing required fields
        
        // When & Then
        mockMvc.perform(post("/api/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        
        verify(authorService, never()).createAuthor(any(AuthorCreateRequest.class));
    }
    
    @Test
    void createAuthor_DuplicateAuthor_Conflict() throws Exception {
        // Given
        when(authorService.createAuthor(any(AuthorCreateRequest.class)))
                .thenThrow(new DuplicateResourceException("Author already exists"));
        
        // When & Then
        mockMvc.perform(post("/api/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict());
        
        verify(authorService).createAuthor(any(AuthorCreateRequest.class));
    }
    
    @Test
    void updateAuthor_Success() throws Exception {
        // Given
        when(authorService.updateAuthor(eq(1L), any(AuthorUpdateRequest.class))).thenReturn(testAuthor);
        
        // When & Then
        mockMvc.perform(put("/api/authors/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("Robert"))
                .andExpect(jsonPath("$.lastName").value("Martin"));
        
        verify(authorService).updateAuthor(eq(1L), any(AuthorUpdateRequest.class));
    }
    
    @Test
    void updateAuthor_NotFound() throws Exception {
        // Given
        when(authorService.updateAuthor(eq(1L), any(AuthorUpdateRequest.class)))
                .thenThrow(new AuthorNotFoundException("Author not found"));
        
        // When & Then
        mockMvc.perform(put("/api/authors/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
        
        verify(authorService).updateAuthor(eq(1L), any(AuthorUpdateRequest.class));
    }
    
    @Test
    void getAuthorById_Success() throws Exception {
        // Given
        when(authorService.getAuthorById(1L)).thenReturn(testAuthor);
        
        // When & Then
        mockMvc.perform(get("/api/authors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("Robert"))
                .andExpect(jsonPath("$.lastName").value("Martin"));
        
        verify(authorService).getAuthorById(1L);
    }
    
    @Test
    void getAuthorById_NotFound() throws Exception {
        // Given
        when(authorService.getAuthorById(1L)).thenThrow(new AuthorNotFoundException("Author not found"));
        
        // When & Then
        mockMvc.perform(get("/api/authors/1"))
                .andExpect(status().isNotFound());
        
        verify(authorService).getAuthorById(1L);
    }
    
    @Test
    void getAllAuthors_Success() throws Exception {
        // Given
        Page<Author> authorPage = new PageImpl<>(Arrays.asList(testAuthor), PageRequest.of(0, 20), 1);
        when(authorService.getAllAuthors(any())).thenReturn(authorPage);
        
        // When & Then
        mockMvc.perform(get("/api/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.totalElements").value(1));
        
        verify(authorService).getAllAuthors(any());
    }
    
    @Test
    void deleteAuthor_Success() throws Exception {
        // Given
        doNothing().when(authorService).deleteAuthor(1L);
        
        // When & Then
        mockMvc.perform(delete("/api/authors/1"))
                .andExpect(status().isNoContent());
        
        verify(authorService).deleteAuthor(1L);
    }
    
    @Test
    void deleteAuthor_NotFound() throws Exception {
        // Given
        doThrow(new AuthorNotFoundException("Author not found")).when(authorService).deleteAuthor(1L);
        
        // When & Then
        mockMvc.perform(delete("/api/authors/1"))
                .andExpect(status().isNotFound());
        
        verify(authorService).deleteAuthor(1L);
    }
    
    @Test
    void addBooksToAuthor_Success() throws Exception {
        // Given
        AuthorBookAssociationRequest request = new AuthorBookAssociationRequest(Set.of(1L));
        when(authorService.addBooksToAuthor(eq(1L), any(AuthorBookAssociationRequest.class))).thenReturn(testAuthor);
        
        // When & Then
        mockMvc.perform(post("/api/authors/1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
        
        verify(authorService).addBooksToAuthor(eq(1L), any(AuthorBookAssociationRequest.class));
    }
    
    @Test
    void removeBooksFromAuthor_Success() throws Exception {
        // Given
        AuthorBookAssociationRequest request = new AuthorBookAssociationRequest(Set.of(1L));
        when(authorService.removeBooksFromAuthor(eq(1L), any(AuthorBookAssociationRequest.class))).thenReturn(testAuthor);
        
        // When & Then
        mockMvc.perform(delete("/api/authors/1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
        
        verify(authorService).removeBooksFromAuthor(eq(1L), any(AuthorBookAssociationRequest.class));
    }
    
    @Test
    void setBooksForAuthor_Success() throws Exception {
        // Given
        AuthorBookAssociationRequest request = new AuthorBookAssociationRequest(Set.of(1L));
        when(authorService.setBooksForAuthor(eq(1L), any(AuthorBookAssociationRequest.class))).thenReturn(testAuthor);
        
        // When & Then
        mockMvc.perform(put("/api/authors/1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
        
        verify(authorService).setBooksForAuthor(eq(1L), any(AuthorBookAssociationRequest.class));
    }
    
    @Test
    void getBooksByAuthor_Success() throws Exception {
        // Given
        when(authorService.getBooksByAuthor(1L)).thenReturn(Set.of(testBook));
        
        // When & Then
        mockMvc.perform(get("/api/authors/1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Clean Code"));
        
        verify(authorService).getBooksByAuthor(1L);
    }
    
    @Test
    void searchAuthors_Success() throws Exception {
        // Given
        AuthorSearchRequest searchRequest = new AuthorSearchRequest();
        searchRequest.setFullName("Robert Martin");
        Page<Author> authorPage = new PageImpl<>(Arrays.asList(testAuthor), PageRequest.of(0, 20), 1);
        when(authorService.searchAuthors(any(AuthorSearchRequest.class), any())).thenReturn(authorPage);
        
        // When & Then
        mockMvc.perform(post("/api/authors/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1L));
        
        verify(authorService).searchAuthors(any(AuthorSearchRequest.class), any());
    }
    
    @Test
    void getAuthorStatistics_Success() throws Exception {
        // Given
        AuthorStatistics statistics = new AuthorStatistics();
        statistics.setAuthorId(1L);
        statistics.setFullName("Robert Martin");
        statistics.setTotalBooks(5L);
        when(authorService.getAuthorStatistics(1L)).thenReturn(statistics);
        
        // When & Then
        mockMvc.perform(get("/api/authors/1/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authorId").value(1L))
                .andExpect(jsonPath("$.fullName").value("Robert Martin"))
                .andExpect(jsonPath("$.totalBooks").value(5));
        
        verify(authorService).getAuthorStatistics(1L);
    }
    
    @Test
    void getMostProlificAuthors_Success() throws Exception {
        // Given
        Page<Author> authorPage = new PageImpl<>(Arrays.asList(testAuthor), PageRequest.of(0, 20), 1);
        when(authorService.getMostProlificAuthors(any())).thenReturn(authorPage);
        
        // When & Then
        mockMvc.perform(get("/api/authors/prolific"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1L));
        
        verify(authorService).getMostProlificAuthors(any());
    }
    
    @Test
    void getRecentlyAddedAuthors_Success() throws Exception {
        // Given
        Page<Author> authorPage = new PageImpl<>(Arrays.asList(testAuthor), PageRequest.of(0, 20), 1);
        when(authorService.getRecentlyAddedAuthors(any())).thenReturn(authorPage);
        
        // When & Then
        mockMvc.perform(get("/api/authors/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1L));
        
        verify(authorService).getRecentlyAddedAuthors(any());
    }
    
    @Test
    void findAuthorsByNationality_Success() throws Exception {
        // Given
        Page<Author> authorPage = new PageImpl<>(Arrays.asList(testAuthor), PageRequest.of(0, 20), 1);
        when(authorService.findAuthorsByNationality(eq("American"), any())).thenReturn(authorPage);
        
        // When & Then
        mockMvc.perform(get("/api/authors/nationality/American"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1L));
        
        verify(authorService).findAuthorsByNationality(eq("American"), any());
    }
    
    @Test
    void findAuthorsByBirthYear_Success() throws Exception {
        // Given
        Page<Author> authorPage = new PageImpl<>(Arrays.asList(testAuthor), PageRequest.of(0, 20), 1);
        when(authorService.findAuthorsByBirthYear(eq(1952), any())).thenReturn(authorPage);
        
        // When & Then
        mockMvc.perform(get("/api/authors/birth-year/1952"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1L));
        
        verify(authorService).findAuthorsByBirthYear(eq(1952), any());
    }
}