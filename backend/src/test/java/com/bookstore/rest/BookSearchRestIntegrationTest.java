package com.bookstore.rest;

import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import com.bookstore.repository.AuthorRepository;
import com.bookstore.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Book Search REST endpoints.
 * Tests the complete request-response cycle including Spring Data REST integration.
 */
class BookSearchRestIntegrationTest extends BaseRestIntegrationTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    private static final String SEARCH_BASE_PATH = BOOKS_PATH + "/search";

    private Book testBook1;
    private Book testBook2;
    private Book testBook3;
    private Author testAuthor1;
    private Author testAuthor2;

    @BeforeEach
    void setUp() {
        // Clean up existing data
        bookRepository.deleteAll();
        authorRepository.deleteAll();

        // Create test authors
        testAuthor1 = new Author("Stephen", "King");
        testAuthor1.setNationality("American");
        testAuthor1.setBiography("Famous horror novelist");
        testAuthor1 = authorRepository.save(testAuthor1);

        testAuthor2 = new Author("Agatha", "Christie");
        testAuthor2.setNationality("British");
        testAuthor2.setBiography("Mystery writer");
        testAuthor2 = authorRepository.save(testAuthor2);

        // Create test books
        testBook1 = new Book("The Shining", "978-0-385-12167-5");
        testBook1.setDescription("A horror novel about a haunted hotel");
        testBook1.setGenre("Horror");
        testBook1.setPublicationYear(1977);
        testBook1.setAvailableCopies(3);
        testBook1.setTotalCopies(5);
        testBook1.addAuthor(testAuthor1);
        testBook1 = bookRepository.save(testBook1);

        testBook2 = new Book("Murder on the Orient Express", "978-0-00-711926-0");
        testBook2.setDescription("A classic mystery novel");
        testBook2.setGenre("Mystery");
        testBook2.setPublicationYear(1934);
        testBook2.setAvailableCopies(0);
        testBook2.setTotalCopies(3);
        testBook2.addAuthor(testAuthor2);
        testBook2 = bookRepository.save(testBook2);

        testBook3 = new Book("IT", "978-0-670-81302-4");
        testBook3.setDescription("A horror novel about a shape-shifting entity");
        testBook3.setGenre("Horror");
        testBook3.setPublicationYear(1986);
        testBook3.setAvailableCopies(2);
        testBook3.setTotalCopies(4);
        testBook3.addAuthor(testAuthor1);
        testBook3 = bookRepository.save(testBook3);
    }

    @Test
    void testSearchByTitle_Integration() throws Exception {
        mockMvc.perform(get(SEARCH_BASE_PATH + "/findByTitle")
                .param("title", "Shining")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("The Shining")))
                .andExpect(jsonPath("$.content[0].isbn", is("978-0-385-12167-5")))
                .andExpect(jsonPath("$.content[0].genre", is("Horror")))
                .andExpect(jsonPath("$.pageable.pageNumber", is(0)))
                .andExpect(jsonPath("$.pageable.pageSize", is(20)))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)));
    }

    @Test
    void testSearchByAuthor_Integration() throws Exception {
        mockMvc.perform(get(SEARCH_BASE_PATH + "/findByAuthor")
                .param("author", "Stephen King")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].title", 
                    containsInAnyOrder("The Shining", "IT")))
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    void testSearchByAuthor_PartialName_Integration() throws Exception {
        mockMvc.perform(get(SEARCH_BASE_PATH + "/findByAuthor")
                .param("author", "King")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].title", 
                    containsInAnyOrder("The Shining", "IT")));
    }

    @Test
    void testSearchByIsbn_Found_Integration() throws Exception {
        mockMvc.perform(get(SEARCH_BASE_PATH + "/findByIsbn")
                .param("isbn", "978-0-385-12167-5")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title", is("The Shining")))
                .andExpect(jsonPath("$.isbn", is("978-0-385-12167-5")))
                .andExpect(jsonPath("$.authors", hasSize(1)));
    }

    @Test
    void testSearchByIsbn_NotFound_Integration() throws Exception {
        mockMvc.perform(get(SEARCH_BASE_PATH + "/findByIsbn")
                .param("isbn", "978-0-000000-00-0")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSearchByGenre_Integration() throws Exception {
        mockMvc.perform(get(SEARCH_BASE_PATH + "/findByGenre")
                .param("genre", "Horror")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].title", 
                    containsInAnyOrder("The Shining", "IT")))
                .andExpect(jsonPath("$.content[*].genre", everyItem(is("Horror"))));
    }

    @Test
    void testAdvancedSearch_MultipleFilters_Integration() throws Exception {
        mockMvc.perform(get(SEARCH_BASE_PATH + "/advanced")
                .param("genre", "Horror")
                .param("author", "Stephen")
                .param("availableOnly", "true")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].title", 
                    containsInAnyOrder("The Shining", "IT")))
                .andExpect(jsonPath("$.content[*].availableCopies", everyItem(greaterThan(0))));
    }

    @Test
    void testAdvancedSearch_AvailableOnly_Integration() throws Exception {
        mockMvc.perform(get(SEARCH_BASE_PATH + "/advanced")
                .param("availableOnly", "true")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].title", 
                    containsInAnyOrder("The Shining", "IT")))
                .andExpect(jsonPath("$.content[*].availableCopies", everyItem(greaterThan(0))));
    }

    @Test
    void testSearchByYear_Integration() throws Exception {
        mockMvc.perform(get(SEARCH_BASE_PATH + "/findByYear")
                .param("year", "1977")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("The Shining")))
                .andExpect(jsonPath("$.content[0].publicationYear", is(1977)));
    }

    @Test
    void testSearchByYearRange_Integration() throws Exception {
        mockMvc.perform(get(SEARCH_BASE_PATH + "/findByYearRange")
                .param("startYear", "1970")
                .param("endYear", "1990")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].title", 
                    containsInAnyOrder("The Shining", "IT")));
    }

    @Test
    void testFindAvailableBooks_Integration() throws Exception {
        mockMvc.perform(get(SEARCH_BASE_PATH + "/available")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].title", 
                    containsInAnyOrder("The Shining", "IT")))
                .andExpect(jsonPath("$.content[*].availableCopies", everyItem(greaterThan(0))));
    }

    @Test
    void testPaginationAndSorting_Integration() throws Exception {
        // Test pagination
        mockMvc.perform(get(SEARCH_BASE_PATH + "/findByTitle")
                .param("title", "")
                .param("page", "0")
                .param("size", "2")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.pageable.pageSize", is(2)))
                .andExpect(jsonPath("$.pageable.pageNumber", is(0)))
                .andExpect(jsonPath("$.totalElements", is(3)));

        // Test sorting by publication year descending
        mockMvc.perform(get(SEARCH_BASE_PATH + "/findByTitle")
                .param("title", "")
                .param("sortBy", "publicationYear")
                .param("sortDir", "desc")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].publicationYear", is(1986)))
                .andExpect(jsonPath("$.content[1].publicationYear", is(1977)))
                .andExpect(jsonPath("$.content[2].publicationYear", is(1934)));
    }

    @Test
    void testSearchWithSpecialCharacters_Integration() throws Exception {
        // Test search with special characters in title
        mockMvc.perform(get(SEARCH_BASE_PATH + "/findByTitle")
                .param("title", "IT")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("IT")));
    }

    @Test
    void testCaseInsensitiveSearch_Integration() throws Exception {
        // Test case insensitive title search
        mockMvc.perform(get(SEARCH_BASE_PATH + "/findByTitle")
                .param("title", "shining")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("The Shining")));

        // Test case insensitive genre search
        mockMvc.perform(get(SEARCH_BASE_PATH + "/findByGenre")
                .param("genre", "horror")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));

        // Test case insensitive author search
        mockMvc.perform(get(SEARCH_BASE_PATH + "/findByAuthor")
                .param("author", "stephen king")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void testEmptySearchResults_Integration() throws Exception {
        mockMvc.perform(get(SEARCH_BASE_PATH + "/findByTitle")
                .param("title", "NonExistentBook")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", is(0)))
                .andExpect(jsonPath("$.totalPages", is(0)));
    }

    @Test
    void testAdvancedSearchWithNoFilters_Integration() throws Exception {
        mockMvc.perform(get(SEARCH_BASE_PATH + "/advanced")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements", is(3)));
    }

    @Test
    void testPopularAndRecentBooks_Integration() throws Exception {
        // Test popular books endpoint
        mockMvc.perform(get(SEARCH_BASE_PATH + "/popular")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements", is(3)));

        // Test recent books endpoint
        mockMvc.perform(get(SEARCH_BASE_PATH + "/recent")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements", is(3)));
    }

    @Test
    void testInvalidSortParameters_Integration() throws Exception {
        // Test with invalid sort field - should default to title
        mockMvc.perform(get(SEARCH_BASE_PATH + "/findByTitle")
                .param("title", "")
                .param("sortBy", "invalidField")
                .param("sortDir", "asc")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)));

        // Test with invalid sort direction - should default to asc
        mockMvc.perform(get(SEARCH_BASE_PATH + "/findByTitle")
                .param("title", "")
                .param("sortBy", "title")
                .param("sortDir", "invalid")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)));
    }
}