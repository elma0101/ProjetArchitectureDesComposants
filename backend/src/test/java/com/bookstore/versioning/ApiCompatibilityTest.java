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

/**
 * Integration tests for API backward compatibility
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class ApiCompatibilityTest {

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
    void testV1ToV2Compatibility() throws Exception {
        // Setup mock data
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Compatibility Test Book");
        book.setIsbn("1234567890");
        book.setGenre("Fiction");
        
        Page<Book> bookPage = new PageImpl<>(Arrays.asList(book));
        when(bookRepository.findAll(any(Pageable.class))).thenReturn(bookPage);

        // Test that V1 client can call V2 endpoint
        mockMvc.perform(get("/api/v2/books")
                .header("API-Version", "1.0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("Compatibility Test Book"));
    }

    @Test
    void testVersionNegotiation() throws Exception {
        // Setup mock data
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Version Negotiation Book");
        book.setIsbn("1234567890");
        
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        // Test version negotiation with different version headers
        mockMvc.perform(get("/api/v1/books/1")
                .header("API-Version", "1.0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Version Negotiation Book"));

        mockMvc.perform(get("/api/v2/books/1")
                .header("API-Version", "2.0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Version Negotiation Book"));
    }

    @Test
    void testMigrationGuideHeaders() throws Exception {
        // Setup mock data
        when(bookRepository.findAll()).thenReturn(Arrays.asList(new Book()));

        // Test that deprecated endpoints provide migration guidance
        mockMvc.perform(get("/api/legacy/books")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Deprecation", "true"))
                .andExpect(header().string("API-Deprecated-Version", "0.9"))
                .andExpect(header().string("API-Deprecated-Since", "2024-01-01"))
                .andExpect(header().string("API-Migration-Guide", "https://docs.bookstore.com/migration/v1"))
                .andExpect(header().exists("Warning"));
    }

    @Test
    void testVersionSpecificFeatures() throws Exception {
        // Setup mock data
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Feature Test Book");
        book.setIsbn("1234567890");
        
        Page<Book> bookPage = new PageImpl<>(Arrays.asList(book));
        when(bookRepository.findAll(any(Pageable.class))).thenReturn(bookPage);

        // Test V1 endpoint (basic functionality)
        mockMvc.perform(get("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        // Test V2 endpoint with enhanced features (search parameters)
        mockMvc.perform(get("/api/v2/books")
                .param("title", "Feature Test")
                .param("available", "true")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testErrorResponseCompatibility() throws Exception {
        // Test that error responses are consistent across versions
        mockMvc.perform(get("/api/v1/books/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v2/books/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testContentNegotiation() throws Exception {
        // Setup mock data
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Content Negotiation Book");
        book.setIsbn("1234567890");
        
        Page<Book> bookPage = new PageImpl<>(Arrays.asList(book));
        when(bookRepository.findAll(any(Pageable.class))).thenReturn(bookPage);

        // Test JSON response
        mockMvc.perform(get("/api/v1/books")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // Test HAL+JSON response (Spring Data REST default)
        mockMvc.perform(get("/api/books")
                .accept("application/hal+json")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}