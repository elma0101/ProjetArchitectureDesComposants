package com.bookstore.rest;

import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import com.bookstore.repository.AuthorRepository;
import com.bookstore.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthorRestIntegrationTest extends BaseRestIntegrationTest {

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    private Author testAuthor;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
        authorRepository.deleteAll();

        // Create test author
        testAuthor = new Author("Jane", "Smith");
        testAuthor.setBiography("A renowned author of fiction novels");
        testAuthor.setBirthDate(LocalDate.of(1975, 5, 15));
        testAuthor.setNationality("British");
        testAuthor = authorRepository.save(testAuthor);
    }

    @Test
    void shouldGetAllAuthors() throws Exception {
        mockMvc.perform(get(AUTHORS_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.authors", hasSize(1)))
                .andExpect(jsonPath("$._embedded.authors[0].firstName", is("Jane")))
                .andExpect(jsonPath("$._embedded.authors[0].lastName", is("Smith")))
                .andExpect(jsonPath("$._embedded.authors[0].nationality", is("British")))
                .andExpect(jsonPath("$.page.size", is(20)))
                .andExpect(jsonPath("$.page.totalElements", is(1)));
    }

    @Test
    void shouldGetAuthorById() throws Exception {
        mockMvc.perform(get(AUTHORS_PATH + "/{id}", testAuthor.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName", is("Jane")))
                .andExpect(jsonPath("$.lastName", is("Smith")))
                .andExpect(jsonPath("$.biography", is("A renowned author of fiction novels")))
                .andExpect(jsonPath("$.nationality", is("British")))
                .andExpect(jsonPath("$.birthDate", is("1975-05-15")));
    }

    @Test
    void shouldCreateNewAuthor() throws Exception {
        Author newAuthor = new Author("John", "Doe");
        newAuthor.setBiography("A new author");
        newAuthor.setBirthDate(LocalDate.of(1980, 1, 1));
        newAuthor.setNationality("American");

        mockMvc.perform(post(AUTHORS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newAuthor)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")))
                .andExpect(jsonPath("$.nationality", is("American")))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    void shouldUpdateExistingAuthor() throws Exception {
        testAuthor.setBiography("Updated biography");
        testAuthor.setNationality("Canadian");

        mockMvc.perform(put(AUTHORS_PATH + "/{id}", testAuthor.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAuthor)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.biography", is("Updated biography")))
                .andExpect(jsonPath("$.nationality", is("Canadian")))
                .andExpect(jsonPath("$.firstName", is("Jane")))
                .andExpect(jsonPath("$.lastName", is("Smith")));
    }

    @Test
    void shouldPartiallyUpdateAuthor() throws Exception {
        String partialUpdate = "{\"nationality\":\"Australian\"}";

        mockMvc.perform(patch(AUTHORS_PATH + "/{id}", testAuthor.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(partialUpdate))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.nationality", is("Australian")))
                .andExpect(jsonPath("$.firstName", is("Jane")))
                .andExpect(jsonPath("$.lastName", is("Smith")));
    }

    @Test
    void shouldDeleteAuthor() throws Exception {
        mockMvc.perform(delete(AUTHORS_PATH + "/{id}", testAuthor.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(AUTHORS_PATH + "/{id}", testAuthor.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldSearchAuthorsByFirstName() throws Exception {
        mockMvc.perform(get(AUTHORS_PATH + "/search/findByFirstNameContainingIgnoreCase")
                .param("firstName", "Jane"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.authors", hasSize(1)))
                .andExpect(jsonPath("$._embedded.authors[0].firstName", is("Jane")));
    }

    @Test
    void shouldSearchAuthorsByLastName() throws Exception {
        mockMvc.perform(get(AUTHORS_PATH + "/search/findByLastNameContainingIgnoreCase")
                .param("lastName", "Smith"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.authors", hasSize(1)))
                .andExpect(jsonPath("$._embedded.authors[0].lastName", is("Smith")));
    }

    @Test
    void shouldSearchAuthorsByFullName() throws Exception {
        mockMvc.perform(get(AUTHORS_PATH + "/search/findByFullNameContainingIgnoreCase")
                .param("name", "Jane Smith"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.authors", hasSize(1)))
                .andExpect(jsonPath("$._embedded.authors[0].firstName", is("Jane")))
                .andExpect(jsonPath("$._embedded.authors[0].lastName", is("Smith")));
    }

    @Test
    void shouldSearchAuthorsByNationality() throws Exception {
        mockMvc.perform(get(AUTHORS_PATH + "/search/findByNationalityContainingIgnoreCase")
                .param("nationality", "British"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.authors", hasSize(1)))
                .andExpect(jsonPath("$._embedded.authors[0].nationality", is("British")));
    }

    @Test
    void shouldGetAuthorBooks() throws Exception {
        // Create a book for the author
        Book book = new Book("Test Book", "978-0-123456-78-9");
        book.setGenre("Fiction");
        book.setTotalCopies(1);
        book.setAvailableCopies(1);
        book.addAuthor(testAuthor);
        bookRepository.save(book);

        mockMvc.perform(get(AUTHORS_PATH + "/{id}/books", testAuthor.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.books", hasSize(1)))
                .andExpect(jsonPath("$._embedded.books[0].title", is("Test Book")));
    }

    @Test
    void shouldHandlePagination() throws Exception {
        // Create additional authors for pagination test
        for (int i = 1; i <= 25; i++) {
            Author author = new Author("Author" + i, "LastName" + i);
            author.setNationality("Test Nationality");
            authorRepository.save(author);
        }

        mockMvc.perform(get(AUTHORS_PATH)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.authors", hasSize(10)))
                .andExpect(jsonPath("$.page.size", is(10)))
                .andExpect(jsonPath("$.page.number", is(0)))
                .andExpect(jsonPath("$.page.totalPages", greaterThan(1)));
    }

    @Test
    void shouldHandleSorting() throws Exception {
        // Create additional author with different name for sorting
        Author anotherAuthor = new Author("Alice", "Johnson");
        anotherAuthor.setNationality("American");
        authorRepository.save(anotherAuthor);

        mockMvc.perform(get(AUTHORS_PATH)
                .param("sort", "firstName,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.authors[0].firstName", is("Alice")))
                .andExpect(jsonPath("$._embedded.authors[1].firstName", is("Jane")));
    }

    @Test
    void shouldReturnNotFoundForNonExistentAuthor() throws Exception {
        mockMvc.perform(get(AUTHORS_PATH + "/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldValidateRequiredFields() throws Exception {
        Author invalidAuthor = new Author();
        // Missing required fields: firstName and lastName

        mockMvc.perform(post(AUTHORS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidAuthor)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors", hasSize(greaterThan(0))));
    }

    @Test
    void shouldValidateBirthDateInPast() throws Exception {
        Author invalidAuthor = new Author("Future", "Author");
        invalidAuthor.setBirthDate(LocalDate.now().plusDays(1)); // Future date

        mockMvc.perform(post(AUTHORS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidAuthor)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'birthDate')].message", 
                    hasItem(containsString("past"))));
    }

    @Test
    void shouldIncludeHATEOASLinks() throws Exception {
        mockMvc.perform(get(AUTHORS_PATH + "/{id}", testAuthor.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href", notNullValue()))
                .andExpect(jsonPath("$._links.author.href", notNullValue()))
                .andExpect(jsonPath("$._links.books.href", notNullValue()));
    }
}