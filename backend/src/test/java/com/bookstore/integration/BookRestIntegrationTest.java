package com.bookstore.integration;

import com.bookstore.dto.BookCreateRequest;
import com.bookstore.dto.BookUpdateRequest;
import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import com.bookstore.repository.AuthorRepository;
import com.bookstore.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive integration tests for Book REST endpoints
 */
@SpringBootTest
class BookRestIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    private Author testAuthor;
    private Book testBook;

    @BeforeEach
    void setUpTestData() {
        // Clean up existing data
        bookRepository.deleteAll();
        authorRepository.deleteAll();

        // Create test author
        testAuthor = new Author();
        testAuthor.setFirstName("John");
        testAuthor.setLastName("Doe");
        testAuthor.setBiography("Test author biography");
        testAuthor.setNationality("American");
        testAuthor = authorRepository.save(testAuthor);

        // Create test book
        testBook = new Book();
        testBook.setTitle("Test Book");
        testBook.setIsbn("978-0123456789");
        testBook.setDescription("A test book description");
        testBook.setPublicationYear(2023);
        testBook.setGenre("Fiction");
        testBook.setAvailableCopies(5);
        testBook.setTotalCopies(10);
        testBook.setAuthors(Set.of(testAuthor));
        testBook = bookRepository.save(testBook);
    }

    @Test
    void shouldGetAllBooks() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.books").isArray())
                .andExpect(jsonPath("$._embedded.books[0].title").value("Test Book"))
                .andExpect(jsonPath("$._embedded.books[0].isbn").value("978-0123456789"))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    void shouldGetBookById() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/{id}", testBook.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("Test Book"))
                .andExpect(jsonPath("$.isbn").value("978-0123456789"))
                .andExpect(jsonPath("$.description").value("A test book description"))
                .andExpect(jsonPath("$.publicationYear").value(2023))
                .andExpect(jsonPath("$.genre").value("Fiction"))
                .andExpect(jsonPath("$.availableCopies").value(5))
                .andExpect(jsonPath("$.totalCopies").value(10));
    }

    @Test
    void shouldCreateNewBook() throws Exception {
        BookCreateRequest request = new BookCreateRequest();
        request.setTitle("New Test Book");
        request.setIsbn("978-0987654321");
        request.setDescription("A new test book");
        request.setPublicationYear(2024);
        request.setGenre("Science Fiction");
        request.setTotalCopies(15);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("New Test Book"))
                .andExpect(jsonPath("$.isbn").value("978-0987654321"))
                .andExpect(jsonPath("$.availableCopies").value(15))
                .andExpect(jsonPath("$.totalCopies").value(15));

        // Verify book was saved to database
        List<Book> books = bookRepository.findAll();
        assertThat(books).hasSize(2);
        assertThat(books.stream().anyMatch(b -> b.getTitle().equals("New Test Book"))).isTrue();
    }

    @Test
    void shouldUpdateExistingBook() throws Exception {
        BookUpdateRequest request = new BookUpdateRequest();
        request.setTitle("Updated Test Book");
        request.setDescription("Updated description");
        request.setGenre("Updated Genre");
        request.setAvailableCopies(3);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/books/{id}", testBook.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("Updated Test Book"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.genre").value("Updated Genre"))
                .andExpect(jsonPath("$.availableCopies").value(3));

        // Verify book was updated in database
        Book updatedBook = bookRepository.findById(testBook.getId()).orElseThrow();
        assertThat(updatedBook.getTitle()).isEqualTo("Updated Test Book");
        assertThat(updatedBook.getDescription()).isEqualTo("Updated description");
        assertThat(updatedBook.getGenre()).isEqualTo("Updated Genre");
        assertThat(updatedBook.getAvailableCopies()).isEqualTo(3);
    }

    @Test
    void shouldDeleteBook() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/books/{id}", testBook.getId()))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Verify book was deleted from database
        assertThat(bookRepository.findById(testBook.getId())).isEmpty();
    }

    @Test
    void shouldReturnNotFoundForNonExistentBook() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/{id}", 99999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldValidateBookCreationRequest() throws Exception {
        BookCreateRequest invalidRequest = new BookCreateRequest();
        // Missing required fields

        mockMvc.perform(MockMvcRequestBuilders.post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldSearchBooksByTitle() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/search/findByTitleContainingIgnoreCase")
                        .param("title", "Test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.books").isArray())
                .andExpect(jsonPath("$._embedded.books[0].title").value("Test Book"));
    }

    @Test
    void shouldSearchBooksByGenre() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/search/findByGenreIgnoreCase")
                        .param("genre", "Fiction")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.books").isArray())
                .andExpect(jsonPath("$._embedded.books[0].genre").value("Fiction"));
    }

    @Test
    void shouldGetBooksWithPagination() throws Exception {
        // Create additional books for pagination test
        for (int i = 1; i <= 25; i++) {
            Book book = new Book();
            book.setTitle("Book " + i);
            book.setIsbn("978-012345678" + String.format("%02d", i));
            book.setDescription("Description " + i);
            book.setPublicationYear(2020 + (i % 5));
            book.setGenre("Genre " + (i % 3));
            book.setAvailableCopies(i);
            book.setTotalCopies(i * 2);
            bookRepository.save(book);
        }

        mockMvc.perform(MockMvcRequestBuilders.get("/api/books")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "title,asc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.books").isArray())
                .andExpect(jsonPath("$._embedded.books.length()").value(10))
                .andExpect(jsonPath("$.page.size").value(10))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(26)) // 25 + 1 original
                .andExpect(jsonPath("$.page.totalPages").value(3));
    }

    @Test
    void shouldHandleHATEOASLinks() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/{id}", testBook.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.book.href").exists())
                .andExpect(jsonPath("$._links.authors.href").exists());
    }

    @Test
    void shouldGetBookAuthors() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/{id}/authors", testBook.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.authors").isArray())
                .andExpect(jsonPath("$._embedded.authors[0].firstName").value("John"))
                .andExpect(jsonPath("$._embedded.authors[0].lastName").value("Doe"));
    }

    @Test
    void shouldHandleConcurrentBookUpdates() throws Exception {
        // Simulate concurrent updates
        BookUpdateRequest request1 = new BookUpdateRequest();
        request1.setTitle("Concurrent Update 1");
        request1.setAvailableCopies(1);

        BookUpdateRequest request2 = new BookUpdateRequest();
        request2.setTitle("Concurrent Update 2");
        request2.setAvailableCopies(2);

        // Both requests should succeed, but the last one should win
        mockMvc.perform(MockMvcRequestBuilders.put("/api/books/{id}", testBook.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request1)))
                .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.put("/api/books/{id}", testBook.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Concurrent Update 2"))
                .andExpect(jsonPath("$.availableCopies").value(2));
    }
}