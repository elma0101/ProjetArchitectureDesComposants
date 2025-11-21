package com.bookstore.versioning;

import com.bookstore.entity.Book;
import com.bookstore.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class ApiVersioningTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private BookRepository bookRepository;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testVersioningWithHeader() throws Exception {
        // Setup mock data
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Test Book");
        book.setIsbn("1234567890");
        
        Page<Book> bookPage = new PageImpl<>(Arrays.asList(book));
        when(bookRepository.findAll(any(Pageable.class))).thenReturn(bookPage);

        // Test V1 API with header
        mockMvc.perform(get("/api/v1/books")
                .header("API-Version", "1.0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("Test Book"));
    }

    @Test
    void testVersioningWithUrlPath() throws Exception {
        // Setup mock data
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Test Book V2");
        book.setIsbn("1234567890");
        
        Page<Book> bookPage = new PageImpl<>(Arrays.asList(book));
        when(bookRepository.findAll(any(Pageable.class))).thenReturn(bookPage);

        // Test V2 API with URL path
        mockMvc.perform(get("/api/v2/books")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testDeprecatedApiWarnings() throws Exception {
        // Setup mock data
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Legacy Book");
        book.setIsbn("1234567890");
        
        when(bookRepository.findAll()).thenReturn(Arrays.asList(book));

        // Test deprecated legacy API
        mockMvc.perform(get("/api/legacy/books")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Deprecation", "true"))
                .andExpect(header().string("API-Deprecated-Version", "0.9"))
                .andExpect(header().string("API-Deprecated-Since", "2024-01-01"))
                .andExpect(header().exists("Warning"));
    }

    @Test
    void testDefaultVersionFallback() throws Exception {
        // Setup mock data
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Default Version Book");
        book.setIsbn("1234567890");
        
        Page<Book> bookPage = new PageImpl<>(Arrays.asList(book));
        when(bookRepository.findAll(any(Pageable.class))).thenReturn(bookPage);

        // Test without specifying version (should default to 1.0)
        mockMvc.perform(get("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testBackwardCompatibility() throws Exception {
        // Setup mock data
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Compatible Book");
        book.setIsbn("1234567890");
        
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        // Test that newer API version can handle older requests
        mockMvc.perform(get("/api/v2/books/1")
                .header("API-Version", "1.0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Compatible Book"));
    }
}