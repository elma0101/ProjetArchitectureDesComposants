package com.bookstore.client;

import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import com.bookstore.repository.AuthorRepository;
import com.bookstore.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.hateoas.PagedModel;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class WebClientIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    private ReactiveBookService reactiveBookService;
    private ReactiveAuthorService reactiveAuthorService;
    private ReactiveExternalService reactiveExternalService;

    @BeforeEach
    void setUp() {
        String baseUrl = "http://localhost:" + port;
        
        WebClient bookWebClient = WebClient.builder().baseUrl(baseUrl).build();
        WebClient authorWebClient = WebClient.builder().baseUrl(baseUrl).build();
        WebClient genericWebClient = WebClient.builder().baseUrl(baseUrl).build();
        
        reactiveBookService = new ReactiveBookService(bookWebClient);
        reactiveAuthorService = new ReactiveAuthorService(authorWebClient);
        reactiveExternalService = new ReactiveExternalService(
                reactiveBookService, reactiveAuthorService, genericWebClient);

        // Clean up and set up test data
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        setupTestData();
    }

    @Test
    void reactiveBookService_ShouldRetrieveBooksFromActualAPI() {
        // When & Then
        StepVerifier.create(reactiveBookService.getAllBooks(0, 10))
                .assertNext(pagedModel -> {
                    assertThat(pagedModel.getContent()).isNotEmpty();
                    assertThat(pagedModel.getContent().size()).isGreaterThan(0);
                    
                    Book firstBook = pagedModel.getContent().iterator().next();
                    assertThat(firstBook.getTitle()).isNotNull();
                    assertThat(firstBook.getIsbn()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void reactiveBookService_ShouldRetrieveSpecificBook() {
        // Given - Get the first book from the repository
        Book savedBook = bookRepository.findAll().iterator().next();

        // When & Then
        StepVerifier.create(reactiveBookService.getBookById(savedBook.getId()))
                .assertNext(book -> {
                    assertThat(book.getId()).isEqualTo(savedBook.getId());
                    assertThat(book.getTitle()).isEqualTo(savedBook.getTitle());
                    assertThat(book.getIsbn()).isEqualTo(savedBook.getIsbn());
                })
                .verifyComplete();
    }

    @Test
    void reactiveAuthorService_ShouldRetrieveAuthorsFromActualAPI() {
        // When & Then
        StepVerifier.create(reactiveAuthorService.getAllAuthors(0, 10))
                .assertNext(pagedModel -> {
                    assertThat(pagedModel.getContent()).isNotEmpty();
                    assertThat(pagedModel.getContent().size()).isGreaterThan(0);
                    
                    Author firstAuthor = pagedModel.getContent().iterator().next();
                    assertThat(firstAuthor.getFirstName()).isNotNull();
                    assertThat(firstAuthor.getLastName()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void reactiveAuthorService_ShouldRetrieveSpecificAuthor() {
        // Given - Get the first author from the repository
        Author savedAuthor = authorRepository.findAll().iterator().next();

        // When & Then
        StepVerifier.create(reactiveAuthorService.getAuthorById(savedAuthor.getId()))
                .assertNext(author -> {
                    assertThat(author.getId()).isEqualTo(savedAuthor.getId());
                    assertThat(author.getFirstName()).isEqualTo(savedAuthor.getFirstName());
                    assertThat(author.getLastName()).isEqualTo(savedAuthor.getLastName());
                })
                .verifyComplete();
    }

    @Test
    void reactiveBookService_ShouldSearchBooksByTitle() {
        // When & Then
        StepVerifier.create(reactiveBookService.findBooksByTitle("Test"))
                .assertNext(pagedModel -> {
                    assertThat(pagedModel.getContent()).isNotEmpty();
                    pagedModel.getContent().forEach(book -> {
                        assertThat(book.getTitle().toLowerCase()).contains("test");
                    });
                })
                .verifyComplete();
    }

    @Test
    void reactiveExternalService_ShouldGetCompleteBookInfo() {
        // Given - Get the first book from the repository
        Book savedBook = bookRepository.findAll().iterator().next();

        // When & Then
        StepVerifier.create(reactiveExternalService.getCompleteBookInfo(savedBook.getId()))
                .assertNext(result -> {
                    assertThat(result).containsKey("book");
                    assertThat(result).containsKey("authors");
                    assertThat(result).containsKey("authorCount");
                    
                    @SuppressWarnings("unchecked")
                    Book resultBook = (Book) result.get("book");
                    assertThat(resultBook.getId()).isEqualTo(savedBook.getId());
                    assertThat(resultBook.getTitle()).isEqualTo(savedBook.getTitle());
                })
                .verifyComplete();
    }

    @Test
    void reactiveExternalService_ShouldGetAuthorWithBooks() {
        // Given - Get the first author from the repository
        Author savedAuthor = authorRepository.findAll().iterator().next();

        // When & Then
        StepVerifier.create(reactiveExternalService.getAuthorWithBooks(savedAuthor.getId()))
                .assertNext(result -> {
                    assertThat(result).containsKey("author");
                    assertThat(result).containsKey("books");
                    assertThat(result).containsKey("bookCount");
                    
                    @SuppressWarnings("unchecked")
                    Author resultAuthor = (Author) result.get("author");
                    assertThat(resultAuthor.getId()).isEqualTo(savedAuthor.getId());
                    assertThat(resultAuthor.getFirstName()).isEqualTo(savedAuthor.getFirstName());
                })
                .verifyComplete();
    }

    @Test
    void reactiveExternalService_ShouldSearchBooksAndAuthors() {
        // When & Then
        StepVerifier.create(reactiveExternalService.searchBooksAndAuthors("Test"))
                .assertNext(result -> {
                    assertThat(result).containsKey("books");
                    assertThat(result).containsKey("authors");
                    assertThat(result).containsKey("bookCount");
                    assertThat(result).containsKey("authorCount");
                    assertThat(result).containsKey("query");
                    
                    assertThat(result.get("query")).isEqualTo("Test");
                    assertThat(result.get("bookCount")).isNotNull();
                    assertThat(result.get("authorCount")).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void reactiveExternalService_ShouldGetPopularContent() {
        // When & Then
        StepVerifier.create(reactiveExternalService.getPopularContent(5))
                .assertNext(result -> {
                    assertThat(result).containsKey("popularBooks");
                    assertThat(result).containsKey("popularAuthors");
                    assertThat(result).containsKey("timestamp");
                    
                    assertThat(result.get("timestamp")).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void reactiveExternalService_ShouldAggregateLibraryData() {
        // When & Then
        StepVerifier.create(reactiveExternalService.aggregateLibraryData())
                .assertNext(result -> {
                    assertThat(result).containsKey("totalBooks");
                    assertThat(result).containsKey("totalAuthors");
                    assertThat(result).containsKey("lastUpdated");
                    assertThat(result).containsKey("status");
                    
                    assertThat(result.get("status")).isEqualTo("active");
                    assertThat(result.get("totalBooks")).isNotNull();
                    assertThat(result.get("totalAuthors")).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void reactiveExternalService_ShouldCheckExternalServicesHealth() {
        // When & Then
        StepVerifier.create(reactiveExternalService.checkExternalServicesHealth())
                .assertNext(result -> {
                    assertThat(result).containsKey("bookService");
                    assertThat(result).containsKey("authorService");
                    assertThat(result).containsKey("overall");
                    assertThat(result).containsKey("timestamp");
                    
                    // Services should be healthy since we're testing against the actual running application
                    assertThat(result.get("bookService")).isEqualTo("healthy");
                    assertThat(result.get("authorService")).isEqualTo("healthy");
                    assertThat(result.get("overall")).isEqualTo("healthy");
                })
                .verifyComplete();
    }

    private void setupTestData() {
        // Create test authors
        Author author1 = new Author();
        author1.setFirstName("Test");
        author1.setLastName("Author1");
        author1.setBiography("Test biography for author 1");
        author1.setBirthDate(LocalDate.of(1980, 1, 1));
        author1.setNationality("American");
        author1.setBooks(new HashSet<>());

        Author author2 = new Author();
        author2.setFirstName("Test");
        author2.setLastName("Author2");
        author2.setBiography("Test biography for author 2");
        author2.setBirthDate(LocalDate.of(1975, 5, 15));
        author2.setNationality("British");
        author2.setBooks(new HashSet<>());

        // Save authors first
        author1 = authorRepository.save(author1);
        author2 = authorRepository.save(author2);

        // Create test books
        Book book1 = new Book();
        book1.setTitle("Test Book 1");
        book1.setIsbn("978-0123456789");
        book1.setDescription("Test description for book 1");
        book1.setGenre("Fiction");
        book1.setPublicationYear(2023);
        book1.setAvailableCopies(5);
        book1.setTotalCopies(10);
        book1.setAuthors(Set.of(author1));

        Book book2 = new Book();
        book2.setTitle("Test Book 2");
        book2.setIsbn("978-0123456790");
        book2.setDescription("Test description for book 2");
        book2.setGenre("Non-Fiction");
        book2.setPublicationYear(2022);
        book2.setAvailableCopies(3);
        book2.setTotalCopies(8);
        book2.setAuthors(Set.of(author2));

        Book book3 = new Book();
        book3.setTitle("Test Collaborative Book");
        book3.setIsbn("978-0123456791");
        book3.setDescription("Test description for collaborative book");
        book3.setGenre("Science");
        book3.setPublicationYear(2024);
        book3.setAvailableCopies(2);
        book3.setTotalCopies(5);
        book3.setAuthors(Set.of(author1, author2));

        // Save books
        bookRepository.save(book1);
        bookRepository.save(book2);
        bookRepository.save(book3);

        // Update author-book relationships
        author1.getBooks().add(book1);
        author1.getBooks().add(book3);
        author2.getBooks().add(book2);
        author2.getBooks().add(book3);

        authorRepository.save(author1);
        authorRepository.save(author2);
    }
}