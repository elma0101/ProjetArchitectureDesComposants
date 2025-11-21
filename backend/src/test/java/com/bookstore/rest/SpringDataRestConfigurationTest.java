package com.bookstore.rest;

import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import com.bookstore.repository.AuthorRepository;
import com.bookstore.repository.BookRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class SpringDataRestConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    private Book testBook;
    private Author testAuthor;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
        authorRepository.deleteAll();

        // Create test author
        testAuthor = new Author("John", "Doe");
        testAuthor.setNationality("American");
        testAuthor = authorRepository.save(testAuthor);

        // Create test book
        testBook = new Book("Test Book", "978-0-123456-78-9");
        testBook.setDescription("A test book for integration testing");
        testBook.setPublicationYear(2023);
        testBook.setGenre("Fiction");
        testBook.setTotalCopies(5);
        testBook.setAvailableCopies(5);
        testBook.addAuthor(testAuthor);
        testBook = bookRepository.save(testBook);
    }

    @Test
    void shouldExposeRepositoryEndpoints() throws Exception {
        // Test that Spring Data REST endpoints are exposed
        mockMvc.perform(get("/api"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(jsonPath("$._links.books.href", notNullValue()))
                .andExpect(jsonPath("$._links.authors.href", notNullValue()))
                .andExpect(jsonPath("$._links.loans.href", notNullValue()))
                .andExpect(jsonPath("$._links.recommendations.href", notNullValue()));
    }

    @Test
    void shouldReturnHATEOASFormat() throws Exception {
        // Test that responses include HATEOAS links
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(jsonPath("$._embedded.books", hasSize(1)))
                .andExpect(jsonPath("$._embedded.books[0]._links.self.href", notNullValue()))
                .andExpect(jsonPath("$._links.self.href", notNullValue()))
                .andExpect(jsonPath("$._links.profile.href", notNullValue()));
    }

    @Test
    void shouldExposeEntityIds() throws Exception {
        // Test that entity IDs are exposed (configured in RestDataConfig)
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.books[0].id", notNullValue()));
    }

    @Test
    void shouldSupportPagination() throws Exception {
        // Test pagination configuration
        mockMvc.perform(get("/api/books")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.size", is(10)))
                .andExpect(jsonPath("$.page.number", is(0)));
    }

    @Test
    void shouldSupportSorting() throws Exception {
        // Test sorting functionality
        mockMvc.perform(get("/api/books")
                .param("sort", "title,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.books", hasSize(1)));
    }

    @Test
    void shouldExposeSearchEndpoints() throws Exception {
        // Test that search endpoints are available
        mockMvc.perform(get("/api/books/search"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(jsonPath("$._links", notNullValue()));
    }

    @Test
    void shouldSupportCustomSearch() throws Exception {
        // Test custom search methods from repository
        mockMvc.perform(get("/api/books/search/findByTitleContainingIgnoreCase")
                .param("title", "Test"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(jsonPath("$._embedded.books", hasSize(1)))
                .andExpect(jsonPath("$._embedded.books[0].title", is("Test Book")));
    }

    @Test
    void shouldCreateBookWithValidation() throws Exception {
        // Test creating a book with validation
        String newBookJson = """
            {
                "title": "New Book",
                "isbn": "978-0-987654-32-1",
                "description": "A new test book",
                "publicationYear": 2024,
                "genre": "Science Fiction",
                "totalCopies": 3,
                "availableCopies": 3
            }
            """;

        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newBookJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(jsonPath("$.title", is("New Book")))
                .andExpect(jsonPath("$.isbn", is("978-0-987654-32-1")))
                .andExpect(jsonPath("$._links.self.href", notNullValue()));
    }

    @Test
    void shouldValidateRequiredFields() throws Exception {
        // Test validation for required fields
        String invalidBookJson = """
            {
                "description": "Book without title and ISBN"
            }
            """;

        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidBookJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldPreventDuplicateISBN() throws Exception {
        // Test that duplicate ISBN is prevented
        String duplicateBookJson = """
            {
                "title": "Duplicate Book",
                "isbn": "978-0-123456-78-9",
                "totalCopies": 1,
                "availableCopies": 1
            }
            """;

        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(duplicateBookJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldSupportEntityLookupByISBN() throws Exception {
        // Test that books can be looked up by ISBN (configured in RestDataConfig)
        mockMvc.perform(get("/api/books/978-0-123456-78-9"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(jsonPath("$.isbn", is("978-0-123456-78-9")))
                .andExpect(jsonPath("$.title", is("Test Book")));
    }

    @Test
    void shouldDisableDeleteForBooks() throws Exception {
        // Test that DELETE is disabled for books (configured in RestDataConfig)
        mockMvc.perform(delete("/api/books/" + testBook.getId()))
                .andExpect(status().isMethodNotAllowed());
    }
}