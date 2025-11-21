package com.bookstore.controller;

import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import com.bookstore.repository.AuthorRepository;
import com.bookstore.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BookSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Book book1;
    private Book book2;
    private Book book3;
    private Author author1;
    private Author author2;

    @BeforeEach
    void setUp() {
        // Clean up existing data
        bookRepository.deleteAll();
        authorRepository.deleteAll();

        // Create test authors
        author1 = new Author("John", "Doe");
        author1.setNationality("American");
        author1 = authorRepository.save(author1);

        author2 = new Author("Jane", "Smith");
        author2.setNationality("British");
        author2 = authorRepository.save(author2);

        // Create test books
        book1 = new Book("The Great Adventure", "978-0-123456-78-9");
        book1.setDescription("An exciting adventure story");
        book1.setGenre("Adventure");
        book1.setPublicationYear(2020);
        book1.setAvailableCopies(5);
        book1.setTotalCopies(10);
        book1.addAuthor(author1);
        book1 = bookRepository.save(book1);

        book2 = new Book("Mystery of the Lost City", "978-0-987654-32-1");
        book2.setDescription("A thrilling mystery novel");
        book2.setGenre("Mystery");
        book2.setPublicationYear(2021);
        book2.setAvailableCopies(0);
        book2.setTotalCopies(5);
        book2.addAuthor(author2);
        book2 = bookRepository.save(book2);

        book3 = new Book("Science Fiction Chronicles", "978-0-555666-77-8");
        book3.setDescription("A collection of sci-fi stories");
        book3.setGenre("Science Fiction");
        book3.setPublicationYear(2019);
        book3.setAvailableCopies(3);
        book3.setTotalCopies(8);
        book3.addAuthor(author1);
        book3.addAuthor(author2);
        book3 = bookRepository.save(book3);
    }

    @Test
    void testFindByTitle_ShouldReturnMatchingBooks() throws Exception {
        mockMvc.perform(get("/api/books/search/findByTitle")
                .param("title", "Adventure")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("The Great Adventure")))
                .andExpect(jsonPath("$.content[0].isbn", is("978-0-123456-78-9")))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.size", is(20)))
                .andExpect(jsonPath("$.number", is(0)));
    }

    @Test
    void testFindByTitle_WithPagination() throws Exception {
        mockMvc.perform(get("/api/books/search/findByTitle")
                .param("title", "")
                .param("page", "0")
                .param("size", "2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.size", is(2)))
                .andExpect(jsonPath("$.number", is(0)));
    }

    @Test
    void testFindByTitle_WithSorting() throws Exception {
        mockMvc.perform(get("/api/books/search/findByTitle")
                .param("title", "")
                .param("sortBy", "publicationYear")
                .param("sortDir", "desc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].publicationYear", is(2021)))
                .andExpect(jsonPath("$.content[1].publicationYear", is(2020)))
                .andExpect(jsonPath("$.content[2].publicationYear", is(2019)));
    }

    @Test
    void testFindByAuthor_ShouldReturnMatchingBooks() throws Exception {
        mockMvc.perform(get("/api/books/search/findByAuthor")
                .param("author", "John Doe")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].title", 
                    containsInAnyOrder("The Great Adventure", "Science Fiction Chronicles")));
    }

    @Test
    void testFindByAuthor_PartialName() throws Exception {
        mockMvc.perform(get("/api/books/search/findByAuthor")
                .param("author", "Jane")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].title", 
                    containsInAnyOrder("Mystery of the Lost City", "Science Fiction Chronicles")));
    }

    @Test
    void testFindByIsbn_ExistingBook() throws Exception {
        mockMvc.perform(get("/api/books/search/findByIsbn")
                .param("isbn", "978-0-123456-78-9")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("The Great Adventure")))
                .andExpect(jsonPath("$.isbn", is("978-0-123456-78-9")));
    }

    @Test
    void testFindByIsbn_NonExistentBook() throws Exception {
        mockMvc.perform(get("/api/books/search/findByIsbn")
                .param("isbn", "978-0-000000-00-0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testFindByGenre_ShouldReturnMatchingBooks() throws Exception {
        mockMvc.perform(get("/api/books/search/findByGenre")
                .param("genre", "Mystery")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("Mystery of the Lost City")))
                .andExpect(jsonPath("$.content[0].genre", is("Mystery")));
    }

    @Test
    void testFindByGenre_PartialMatch() throws Exception {
        mockMvc.perform(get("/api/books/search/findByGenre")
                .param("genre", "Science")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("Science Fiction Chronicles")));
    }

    @Test
    void testAdvancedSearch_MultipleFilters() throws Exception {
        mockMvc.perform(get("/api/books/search/advanced")
                .param("genre", "Adventure")
                .param("publicationYear", "2020")
                .param("availableOnly", "true")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("The Great Adventure")));
    }

    @Test
    void testAdvancedSearch_AvailableOnlyFilter() throws Exception {
        mockMvc.perform(get("/api/books/search/advanced")
                .param("availableOnly", "true")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].title", 
                    containsInAnyOrder("The Great Adventure", "Science Fiction Chronicles")));
    }

    @Test
    void testAdvancedSearch_NoFilters() throws Exception {
        mockMvc.perform(get("/api/books/search/advanced")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements", is(3)));
    }

    @Test
    void testFindByYear_ShouldReturnMatchingBooks() throws Exception {
        mockMvc.perform(get("/api/books/search/findByYear")
                .param("year", "2020")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("The Great Adventure")))
                .andExpect(jsonPath("$.content[0].publicationYear", is(2020)));
    }

    @Test
    void testFindByYearRange_ShouldReturnMatchingBooks() throws Exception {
        mockMvc.perform(get("/api/books/search/findByYearRange")
                .param("startYear", "2019")
                .param("endYear", "2020")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].title", 
                    containsInAnyOrder("The Great Adventure", "Science Fiction Chronicles")));
    }

    @Test
    void testFindAvailableBooks_ShouldReturnOnlyAvailableBooks() throws Exception {
        mockMvc.perform(get("/api/books/search/available")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].title", 
                    containsInAnyOrder("The Great Adventure", "Science Fiction Chronicles")))
                .andExpect(jsonPath("$.content[*].availableCopies", everyItem(greaterThan(0))));
    }

    @Test
    void testFindPopularBooks_ShouldReturnBooksOrderedByPopularity() throws Exception {
        mockMvc.perform(get("/api/books/search/popular")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements", is(3)));
    }

    @Test
    void testFindRecentBooks_ShouldReturnBooksOrderedByCreationDate() throws Exception {
        mockMvc.perform(get("/api/books/search/recent")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements", is(3)));
    }

    @Test
    void testSearchWithInvalidSortField_ShouldUseDefaultSort() throws Exception {
        mockMvc.perform(get("/api/books/search/findByTitle")
                .param("title", "")
                .param("sortBy", "invalidField")
                .param("sortDir", "asc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)));
    }

    @Test
    void testSearchWithEmptyResults() throws Exception {
        mockMvc.perform(get("/api/books/search/findByTitle")
                .param("title", "NonExistentTitle")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", is(0)));
    }

    @Test
    void testAdvancedSearch_WithTitleAndAuthor() throws Exception {
        mockMvc.perform(get("/api/books/search/advanced")
                .param("title", "Science")
                .param("author", "John")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("Science Fiction Chronicles")));
    }

    @Test
    void testPaginationEdgeCases() throws Exception {
        // Test page beyond available data
        mockMvc.perform(get("/api/books/search/findByTitle")
                .param("title", "")
                .param("page", "10")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.number", is(10)))
                .andExpect(jsonPath("$.totalElements", is(3)));
    }

    @Test
    void testSortingByDifferentFields() throws Exception {
        // Test sorting by genre
        mockMvc.perform(get("/api/books/search/findByTitle")
                .param("title", "")
                .param("sortBy", "genre")
                .param("sortDir", "asc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].genre", is("Adventure")));

        // Test sorting by available copies
        mockMvc.perform(get("/api/books/search/findByTitle")
                .param("title", "")
                .param("sortBy", "availableCopies")
                .param("sortDir", "desc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].availableCopies", is(5)));
    }
}