package com.bookstore.integration;

import com.bookstore.dto.AuthorCreateRequest;
import com.bookstore.dto.AuthorUpdateRequest;
import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import com.bookstore.repository.AuthorRepository;
import com.bookstore.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive integration tests for Author REST endpoints
 */
@SpringBootTest
class AuthorRestIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    private Author testAuthor;
    private Book testBook;

    @BeforeEach
    void setUpTestData() {
        // Clean up existing data
        bookRepository.deleteAll();
        authorRepository.deleteAll();

        // Create test author
        testAuthor = new Author();
        testAuthor.setFirstName("Jane");
        testAuthor.setLastName("Smith");
        testAuthor.setBiography("Renowned science fiction author");
        testAuthor.setBirthDate(LocalDate.of(1975, 5, 15));
        testAuthor.setNationality("British");
        testAuthor = authorRepository.save(testAuthor);

        // Create test book associated with author
        testBook = new Book();
        testBook.setTitle("Author's Book");
        testBook.setIsbn("978-1234567890");
        testBook.setDescription("A book by the test author");
        testBook.setPublicationYear(2022);
        testBook.setGenre("Science Fiction");
        testBook.setAvailableCopies(3);
        testBook.setTotalCopies(5);
        testBook.setAuthors(Set.of(testAuthor));
        testBook = bookRepository.save(testBook);

        // Update author with book relationship
        testAuthor.setBooks(Set.of(testBook));
        testAuthor = authorRepository.save(testAuthor);
    }

    @Test
    void shouldGetAllAuthors() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.authors").isArray())
                .andExpect(jsonPath("$._embedded.authors[0].firstName").value("Jane"))
                .andExpect(jsonPath("$._embedded.authors[0].lastName").value("Smith"))
                .andExpect(jsonPath("$._embedded.authors[0].nationality").value("British"))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    void shouldGetAuthorById() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/authors/{id}", testAuthor.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.biography").value("Renowned science fiction author"))
                .andExpect(jsonPath("$.nationality").value("British"))
                .andExpect(jsonPath("$.birthDate").value("1975-05-15"));
    }

    @Test
    void shouldCreateNewAuthor() throws Exception {
        AuthorCreateRequest request = new AuthorCreateRequest();
        request.setFirstName("Robert");
        request.setLastName("Johnson");
        request.setBiography("Mystery writer");
        request.setBirthDate(LocalDate.of(1980, 3, 20));
        request.setNationality("American");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value("Robert"))
                .andExpect(jsonPath("$.lastName").value("Johnson"))
                .andExpect(jsonPath("$.biography").value("Mystery writer"))
                .andExpect(jsonPath("$.nationality").value("American"))
                .andExpect(jsonPath("$.birthDate").value("1980-03-20"));

        // Verify author was saved to database
        List<Author> authors = authorRepository.findAll();
        assertThat(authors).hasSize(2);
        assertThat(authors.stream().anyMatch(a -> a.getFirstName().equals("Robert"))).isTrue();
    }

    @Test
    void shouldUpdateExistingAuthor() throws Exception {
        AuthorUpdateRequest request = new AuthorUpdateRequest();
        request.setFirstName("Jane Updated");
        request.setBiography("Updated biography for Jane");
        request.setNationality("Canadian");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/authors/{id}", testAuthor.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value("Jane Updated"))
                .andExpect(jsonPath("$.lastName").value("Smith")) // Should remain unchanged
                .andExpect(jsonPath("$.biography").value("Updated biography for Jane"))
                .andExpect(jsonPath("$.nationality").value("Canadian"));

        // Verify author was updated in database
        Author updatedAuthor = authorRepository.findById(testAuthor.getId()).orElseThrow();
        assertThat(updatedAuthor.getFirstName()).isEqualTo("Jane Updated");
        assertThat(updatedAuthor.getBiography()).isEqualTo("Updated biography for Jane");
        assertThat(updatedAuthor.getNationality()).isEqualTo("Canadian");
    }

    @Test
    void shouldDeleteAuthor() throws Exception {
        // First remove book associations to avoid constraint violations
        testBook.getAuthors().clear();
        bookRepository.save(testBook);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/authors/{id}", testAuthor.getId()))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Verify author was deleted from database
        assertThat(authorRepository.findById(testAuthor.getId())).isEmpty();
    }

    @Test
    void shouldReturnNotFoundForNonExistentAuthor() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/authors/{id}", 99999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldValidateAuthorCreationRequest() throws Exception {
        AuthorCreateRequest invalidRequest = new AuthorCreateRequest();
        // Missing required fields

        mockMvc.perform(MockMvcRequestBuilders.post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldSearchAuthorsByName() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/authors/search/findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase")
                        .param("firstName", "Jane")
                        .param("lastName", "Jane")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.authors").isArray())
                .andExpect(jsonPath("$._embedded.authors[0].firstName").value("Jane"));
    }

    @Test
    void shouldSearchAuthorsByNationality() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/authors/search/findByNationalityIgnoreCase")
                        .param("nationality", "British")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.authors").isArray())
                .andExpect(jsonPath("$._embedded.authors[0].nationality").value("British"));
    }

    @Test
    void shouldGetAuthorBooks() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/authors/{id}/books", testAuthor.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.books").isArray())
                .andExpect(jsonPath("$._embedded.books[0].title").value("Author's Book"))
                .andExpect(jsonPath("$._embedded.books[0].isbn").value("978-1234567890"));
    }

    @Test
    void shouldGetAuthorsWithPagination() throws Exception {
        // Create additional authors for pagination test
        for (int i = 1; i <= 25; i++) {
            Author author = new Author();
            author.setFirstName("Author" + i);
            author.setLastName("LastName" + i);
            author.setBiography("Biography " + i);
            author.setBirthDate(LocalDate.of(1970 + (i % 30), (i % 12) + 1, (i % 28) + 1));
            author.setNationality("Country" + (i % 5));
            authorRepository.save(author);
        }

        mockMvc.perform(MockMvcRequestBuilders.get("/api/authors")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "lastName,asc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.authors").isArray())
                .andExpect(jsonPath("$._embedded.authors.length()").value(10))
                .andExpect(jsonPath("$.page.size").value(10))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(26)) // 25 + 1 original
                .andExpect(jsonPath("$.page.totalPages").value(3));
    }

    @Test
    void shouldHandleHATEOASLinks() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/authors/{id}", testAuthor.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.author.href").exists())
                .andExpect(jsonPath("$._links.books.href").exists());
    }

    @Test
    void shouldHandleAuthorBookRelationships() throws Exception {
        // Create a new book
        Book newBook = new Book();
        newBook.setTitle("Second Book");
        newBook.setIsbn("978-0987654321");
        newBook.setDescription("Second book by author");
        newBook.setPublicationYear(2023);
        newBook.setGenre("Fantasy");
        newBook.setAvailableCopies(2);
        newBook.setTotalCopies(4);
        newBook = bookRepository.save(newBook);

        // Associate book with author via PATCH request
        String bookUri = getUrl("/api/books/" + newBook.getId());
        
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/authors/{id}/books", testAuthor.getId())
                        .contentType("text/uri-list")
                        .content(bookUri))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Verify the relationship was created
        mockMvc.perform(MockMvcRequestBuilders.get("/api/authors/{id}/books", testAuthor.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.books.length()").value(2));
    }

    @Test
    void shouldHandleConcurrentAuthorUpdates() throws Exception {
        AuthorUpdateRequest request1 = new AuthorUpdateRequest();
        request1.setFirstName("Concurrent1");
        request1.setBiography("Biography 1");

        AuthorUpdateRequest request2 = new AuthorUpdateRequest();
        request2.setFirstName("Concurrent2");
        request2.setBiography("Biography 2");

        // Both requests should succeed, but the last one should win
        mockMvc.perform(MockMvcRequestBuilders.put("/api/authors/{id}", testAuthor.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request1)))
                .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.put("/api/authors/{id}", testAuthor.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Concurrent2"))
                .andExpect(jsonPath("$.biography").value("Biography 2"));
    }
}