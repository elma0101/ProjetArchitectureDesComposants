package com.bookstore.rest;

import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import com.bookstore.repository.AuthorRepository;
import com.bookstore.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.http.MediaType;

public class BookRestIntegrationTest extends BaseRestIntegrationTest {

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
    void shouldGetAllBooks() throws Exception {
        mockMvc.perform(get(BOOKS_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(jsonPath("$._embedded.books", hasSize(1)))
                .andExpect(jsonPath("$._embedded.books[0].title", is("Test Book")))
                .andExpect(jsonPath("$._embedded.books[0].isbn", is("978-0-123456-78-9")))
                .andExpect(jsonPath("$.page.size", is(20)))
                .andExpect(jsonPath("$.page.totalElements", is(1)));
    }

    @Test
    void shouldGetBookById() throws Exception {
        mockMvc.perform(get(BOOKS_PATH + "/{id}", testBook.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title", is("Test Book")))
                .andExpect(jsonPath("$.isbn", is("978-0-123456-78-9")))
                .andExpect(jsonPath("$.description", is("A test book for integration testing")))
                .andExpect(jsonPath("$.publicationYear", is(2023)))
                .andExpect(jsonPath("$.genre", is("Fiction")))
                .andExpect(jsonPath("$.totalCopies", is(5)))
                .andExpect(jsonPath("$.availableCopies", is(5)));
    }

    @Test
    void shouldCreateNewBook() throws Exception {
        Book newBook = new Book("New Test Book", "978-0-987654-32-1");
        newBook.setDescription("Another test book");
        newBook.setPublicationYear(2024);
        newBook.setGenre("Science Fiction");
        newBook.setTotalCopies(3);
        newBook.setAvailableCopies(3);

        mockMvc.perform(post(BOOKS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newBook)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title", is("New Test Book")))
                .andExpect(jsonPath("$.isbn", is("978-0-987654-32-1")))
                .andExpect(jsonPath("$.genre", is("Science Fiction")))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    void shouldUpdateExistingBook() throws Exception {
        testBook.setTitle("Updated Test Book");
        testBook.setGenre("Updated Fiction");

        mockMvc.perform(put(BOOKS_PATH + "/{id}", testBook.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testBook)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title", is("Updated Test Book")))
                .andExpect(jsonPath("$.genre", is("Updated Fiction")))
                .andExpect(jsonPath("$.isbn", is("978-0-123456-78-9")));
    }

    @Test
    void shouldPartiallyUpdateBook() throws Exception {
        String partialUpdate = "{\"title\":\"Partially Updated Book\"}";

        mockMvc.perform(patch(BOOKS_PATH + "/{id}", testBook.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(partialUpdate))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title", is("Partially Updated Book")))
                .andExpect(jsonPath("$.isbn", is("978-0-123456-78-9")))
                .andExpect(jsonPath("$.genre", is("Fiction")));
    }

    @Test
    void shouldSearchBooksByTitle() throws Exception {
        mockMvc.perform(get(BOOKS_PATH + "/search/findByTitleContainingIgnoreCase")
                .param("title", "Test"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.books", hasSize(1)))
                .andExpect(jsonPath("$._embedded.books[0].title", is("Test Book")));
    }

    @Test
    void shouldSearchBooksByGenre() throws Exception {
        mockMvc.perform(get(BOOKS_PATH + "/search/findByGenreContainingIgnoreCase")
                .param("genre", "Fiction"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.books", hasSize(1)))
                .andExpect(jsonPath("$._embedded.books[0].genre", is("Fiction")));
    }

    @Test
    void shouldSearchBooksByAuthor() throws Exception {
        mockMvc.perform(get(BOOKS_PATH + "/search/findByAuthorNameContainingIgnoreCase")
                .param("authorName", "John Doe"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.books", hasSize(1)))
                .andExpect(jsonPath("$._embedded.books[0].title", is("Test Book")));
    }

    @Test
    void shouldFindAvailableBooks() throws Exception {
        mockMvc.perform(get(BOOKS_PATH + "/search/findAvailableBooks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.books", hasSize(1)))
                .andExpect(jsonPath("$._embedded.books[0].availableCopies", greaterThan(0)));
    }

    @Test
    void shouldHandlePagination() throws Exception {
        // Create additional books for pagination test
        for (int i = 1; i <= 25; i++) {
            Book book = new Book("Book " + i, "978-0-123456-" + String.format("%02d", i) + "-0");
            book.setGenre("Test Genre");
            book.setTotalCopies(1);
            book.setAvailableCopies(1);
            bookRepository.save(book);
        }

        mockMvc.perform(get(BOOKS_PATH)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.books", hasSize(10)))
                .andExpect(jsonPath("$.page.size", is(10)))
                .andExpect(jsonPath("$.page.number", is(0)))
                .andExpect(jsonPath("$.page.totalPages", greaterThan(1)));
    }

    @Test
    void shouldHandleSorting() throws Exception {
        // Create additional book with different title for sorting
        Book anotherBook = new Book("Another Book", "978-0-111111-11-1");
        anotherBook.setGenre("Fiction");
        anotherBook.setTotalCopies(1);
        anotherBook.setAvailableCopies(1);
        bookRepository.save(anotherBook);

        mockMvc.perform(get(BOOKS_PATH)
                .param("sort", "title,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.books[0].title", is("Another Book")))
                .andExpect(jsonPath("$._embedded.books[1].title", is("Test Book")));
    }

    @Test
    void shouldReturnNotFoundForNonExistentBook() throws Exception {
        mockMvc.perform(get(BOOKS_PATH + "/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldValidateRequiredFields() throws Exception {
        Book invalidBook = new Book();
        // Missing required fields: title and isbn

        mockMvc.perform(post(BOOKS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBook)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors", hasSize(greaterThan(0))));
    }

    @Test
    void shouldValidateISBNFormat() throws Exception {
        Book invalidBook = new Book("Invalid ISBN Book", "invalid-isbn");
        invalidBook.setTotalCopies(1);
        invalidBook.setAvailableCopies(1);

        mockMvc.perform(post(BOOKS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBook)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'isbn')].message", 
                    hasItem(containsString("Invalid ISBN format"))));
    }

    @Test
    void shouldPreventDuplicateISBN() throws Exception {
        Book duplicateBook = new Book("Duplicate Book", "978-0-123456-78-9"); // Same ISBN as testBook
        duplicateBook.setTotalCopies(1);
        duplicateBook.setAvailableCopies(1);

        mockMvc.perform(post(BOOKS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateBook)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    void shouldIncludeHATEOASLinks() throws Exception {
        mockMvc.perform(get(BOOKS_PATH + "/{id}", testBook.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href", notNullValue()))
                .andExpect(jsonPath("$._links.book.href", notNullValue()));
    }
}