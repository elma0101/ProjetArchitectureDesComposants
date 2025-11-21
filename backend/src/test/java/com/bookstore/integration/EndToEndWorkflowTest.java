package com.bookstore.integration;

import com.bookstore.dto.AuthorCreateRequest;
import com.bookstore.dto.BookCreateRequest;
import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import com.bookstore.entity.Loan;
import com.bookstore.entity.LoanStatus;
import com.bookstore.repository.AuthorRepository;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end workflow tests that simulate complete user scenarios
 */
@SpringBootTest
@Transactional
class EndToEndWorkflowTest extends BaseIntegrationTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private LoanRepository loanRepository;

    @BeforeEach
    void cleanUp() {
        loanRepository.deleteAll();
        bookRepository.deleteAll();
        authorRepository.deleteAll();
    }

    @Test
    void shouldCompleteFullBookManagementWorkflow() throws Exception {
        // Step 1: Create an author
        AuthorCreateRequest authorRequest = new AuthorCreateRequest();
        authorRequest.setFirstName("J.K.");
        authorRequest.setLastName("Rowling");
        authorRequest.setBiography("British author, best known for the Harry Potter series");
        authorRequest.setBirthDate(LocalDate.of(1965, 7, 31));
        authorRequest.setNationality("British");

        String authorResponse = mockMvc.perform(MockMvcRequestBuilders.post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(authorRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("J.K."))
                .andExpect(jsonPath("$.lastName").value("Rowling"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Author createdAuthor = fromJson(authorResponse, Author.class);
        Long authorId = createdAuthor.getId();

        // Step 2: Create a book
        BookCreateRequest bookRequest = new BookCreateRequest();
        bookRequest.setTitle("Harry Potter and the Philosopher's Stone");
        bookRequest.setIsbn("978-0747532699");
        bookRequest.setDescription("The first book in the Harry Potter series");
        bookRequest.setPublicationYear(1997);
        bookRequest.setGenre("Fantasy");
        bookRequest.setTotalCopies(10);

        String bookResponse = mockMvc.perform(MockMvcRequestBuilders.post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(bookRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Harry Potter and the Philosopher's Stone"))
                .andExpect(jsonPath("$.isbn").value("978-0747532699"))
                .andExpect(jsonPath("$.availableCopies").value(10))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Book createdBook = fromJson(bookResponse, Book.class);
        Long bookId = createdBook.getId();

        // Step 3: Associate book with author
        String authorUri = getUrl("/api/authors/" + authorId);
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/books/{id}/authors", bookId)
                        .contentType("text/uri-list")
                        .content(authorUri))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Step 4: Verify the association
        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/{id}/authors", bookId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.authors[0].firstName").value("J.K."))
                .andExpect(jsonPath("$._embedded.authors[0].lastName").value("Rowling"));

        // Step 5: Search for the book
        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/search/findByTitleContainingIgnoreCase")
                        .param("title", "Harry Potter")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.books[0].title").value("Harry Potter and the Philosopher's Stone"));

        // Step 6: Create a loan for the book
        Map<String, Object> loanRequest = new HashMap<>();
        loanRequest.put("borrowerName", "Hermione Granger");
        loanRequest.put("borrowerEmail", "hermione@hogwarts.edu");
        loanRequest.put("borrowerId", "STUDENT001");
        loanRequest.put("loanDate", LocalDate.now().toString());
        loanRequest.put("dueDate", LocalDate.now().plusDays(14).toString());
        loanRequest.put("status", "ACTIVE");
        loanRequest.put("notes", "First loan of this book");
        loanRequest.put("book", "/api/books/" + bookId);

        String loanResponse = mockMvc.perform(MockMvcRequestBuilders.post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(loanRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.borrowerName").value("Hermione Granger"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Loan createdLoan = fromJson(loanResponse, Loan.class);
        Long loanId = createdLoan.getId();

        // Step 7: Verify book availability decreased
        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/{id}", bookId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCopies").value(9)); // Should be decreased by 1

        // Step 8: Return the book
        Map<String, Object> returnRequest = new HashMap<>();
        returnRequest.put("status", "RETURNED");
        returnRequest.put("returnDate", LocalDate.now().toString());

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/loans/{id}", loanId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(returnRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"))
                .andExpect(jsonPath("$.returnDate").value(LocalDate.now().toString()));

        // Step 9: Verify book availability increased
        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/{id}", bookId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCopies").value(10)); // Should be back to original

        // Step 10: Verify complete workflow in database
        Book finalBook = bookRepository.findById(bookId).orElseThrow();
        Author finalAuthor = authorRepository.findById(authorId).orElseThrow();
        Loan finalLoan = loanRepository.findById(loanId).orElseThrow();

        assertThat(finalBook.getTitle()).isEqualTo("Harry Potter and the Philosopher's Stone");
        assertThat(finalBook.getAvailableCopies()).isEqualTo(10);
        assertThat(finalBook.getAuthors()).hasSize(1);
        assertThat(finalBook.getAuthors().iterator().next().getLastName()).isEqualTo("Rowling");

        assertThat(finalAuthor.getBooks()).hasSize(1);
        assertThat(finalAuthor.getBooks().iterator().next().getTitle()).contains("Harry Potter");

        assertThat(finalLoan.getStatus()).isEqualTo(LoanStatus.RETURNED);
        assertThat(finalLoan.getReturnDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void shouldHandleMultipleConcurrentLoansWorkflow() throws Exception {
        // Create a book with multiple copies
        BookCreateRequest bookRequest = new BookCreateRequest();
        bookRequest.setTitle("Popular Book");
        bookRequest.setIsbn("978-1234567890");
        bookRequest.setDescription("A very popular book");
        bookRequest.setPublicationYear(2023);
        bookRequest.setGenre("Fiction");
        bookRequest.setTotalCopies(3);

        String bookResponse = mockMvc.perform(MockMvcRequestBuilders.post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(bookRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Book createdBook = fromJson(bookResponse, Book.class);
        Long bookId = createdBook.getId();

        // Create multiple loans concurrently
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> loanRequest = new HashMap<>();
            loanRequest.put("borrowerName", "Borrower " + i);
            loanRequest.put("borrowerEmail", "borrower" + i + "@example.com");
            loanRequest.put("borrowerId", "BORROWER" + String.format("%03d", i));
            loanRequest.put("loanDate", LocalDate.now().toString());
            loanRequest.put("dueDate", LocalDate.now().plusDays(14).toString());
            loanRequest.put("status", "ACTIVE");
            loanRequest.put("book", "/api/books/" + bookId);

            mockMvc.perform(MockMvcRequestBuilders.post("/api/loans")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(loanRequest)))
                    .andExpect(status().isCreated());
        }

        // Verify all copies are loaned out
        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/{id}", bookId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCopies").value(0));

        // Try to create another loan (should fail or be queued)
        Map<String, Object> extraLoanRequest = new HashMap<>();
        extraLoanRequest.put("borrowerName", "Extra Borrower");
        extraLoanRequest.put("borrowerEmail", "extra@example.com");
        extraLoanRequest.put("borrowerId", "EXTRA001");
        extraLoanRequest.put("loanDate", LocalDate.now().toString());
        extraLoanRequest.put("dueDate", LocalDate.now().plusDays(14).toString());
        extraLoanRequest.put("status", "ACTIVE");
        extraLoanRequest.put("book", "/api/books/" + bookId);

        // This should either fail or create a waiting list entry
        mockMvc.perform(MockMvcRequestBuilders.post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(extraLoanRequest)))
                .andExpect(status().isBadRequest()); // Assuming business rule prevents over-borrowing

        // Return one book
        List<Loan> activeLoans = loanRepository.findActiveLoansByBookId(bookId);
        assertThat(activeLoans).hasSize(3);

        Loan firstLoan = activeLoans.get(0);
        Map<String, Object> returnRequest = new HashMap<>();
        returnRequest.put("status", "RETURNED");
        returnRequest.put("returnDate", LocalDate.now().toString());

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/loans/{id}", firstLoan.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(returnRequest)))
                .andExpect(status().isOk());

        // Verify availability increased
        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/{id}", bookId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCopies").value(1));
    }

    @Test
    void shouldHandleOverdueLoanWorkflow() throws Exception {
        // Create a book
        BookCreateRequest bookRequest = new BookCreateRequest();
        bookRequest.setTitle("Overdue Book");
        bookRequest.setIsbn("978-9999999999");
        bookRequest.setDescription("A book that will become overdue");
        bookRequest.setPublicationYear(2023);
        bookRequest.setGenre("Fiction");
        bookRequest.setTotalCopies(1);

        String bookResponse = mockMvc.perform(MockMvcRequestBuilders.post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(bookRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Book createdBook = fromJson(bookResponse, Book.class);
        Long bookId = createdBook.getId();

        // Create a loan with past due date
        Map<String, Object> loanRequest = new HashMap<>();
        loanRequest.put("borrowerName", "Forgetful Borrower");
        loanRequest.put("borrowerEmail", "forgetful@example.com");
        loanRequest.put("borrowerId", "FORGETFUL001");
        loanRequest.put("loanDate", LocalDate.now().minusDays(20).toString());
        loanRequest.put("dueDate", LocalDate.now().minusDays(5).toString()); // Past due
        loanRequest.put("status", "OVERDUE");
        loanRequest.put("book", "/api/books/" + bookId);

        String loanResponse = mockMvc.perform(MockMvcRequestBuilders.post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(loanRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Loan overdueLoan = fromJson(loanResponse, Loan.class);

        // Search for overdue loans
        mockMvc.perform(MockMvcRequestBuilders.get("/api/loans/search/findByStatus")
                        .param("status", "OVERDUE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.loans[0].borrowerName").value("Forgetful Borrower"))
                .andExpect(jsonPath("$._embedded.loans[0].status").value("OVERDUE"));

        // Return the overdue book
        Map<String, Object> returnRequest = new HashMap<>();
        returnRequest.put("status", "RETURNED");
        returnRequest.put("returnDate", LocalDate.now().toString());

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/loans/{id}", overdueLoan.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(returnRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"));

        // Verify no more overdue loans
        mockMvc.perform(MockMvcRequestBuilders.get("/api/loans/search/findByStatus")
                        .param("status", "OVERDUE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.loans").isEmpty());
    }

    @Test
    void shouldHandleComplexSearchAndFilterWorkflow() throws Exception {
        // Create multiple authors
        Author author1 = createAndSaveAuthor("Stephen", "King", "Horror");
        Author author2 = createAndSaveAuthor("Agatha", "Christie", "Mystery");
        Author author3 = createAndSaveAuthor("Isaac", "Asimov", "Science Fiction");

        // Create multiple books with different genres and years
        createAndSaveBook("The Shining", "978-1111111111", "Horror", 1977, author1);
        createAndSaveBook("Murder on the Orient Express", "978-2222222222", "Mystery", 1934, author2);
        createAndSaveBook("Foundation", "978-3333333333", "Science Fiction", 1951, author3);
        createAndSaveBook("It", "978-4444444444", "Horror", 1986, author1);
        createAndSaveBook("The ABC Murders", "978-5555555555", "Mystery", 1936, author2);

        // Search by genre
        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/search/findByGenreIgnoreCase")
                        .param("genre", "Horror")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.books.length()").value(2));

        // Search by author
        mockMvc.perform(MockMvcRequestBuilders.get("/api/authors/search/findByLastNameContainingIgnoreCase")
                        .param("lastName", "King")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.authors[0].firstName").value("Stephen"));

        // Get books by specific author
        mockMvc.perform(MockMvcRequestBuilders.get("/api/authors/{id}/books", author2.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.books.length()").value(2));

        // Search books by title
        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/search/findByTitleContainingIgnoreCase")
                        .param("title", "Murder")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.books[0].title").value("Murder on the Orient Express"));

        // Test pagination with sorting
        mockMvc.perform(MockMvcRequestBuilders.get("/api/books")
                        .param("page", "0")
                        .param("size", "3")
                        .param("sort", "publicationYear,asc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.books.length()").value(3))
                .andExpect(jsonPath("$._embedded.books[0].publicationYear").value(1934)) // Oldest first
                .andExpect(jsonPath("$.page.totalElements").value(5));
    }

    private Author createAndSaveAuthor(String firstName, String lastName, String genre) {
        Author author = new Author();
        author.setFirstName(firstName);
        author.setLastName(lastName);
        author.setBiography(genre + " author");
        author.setBirthDate(LocalDate.of(1900, 1, 1));
        author.setNationality("American");
        return authorRepository.save(author);
    }

    private Book createAndSaveBook(String title, String isbn, String genre, int year, Author author) {
        Book book = new Book();
        book.setTitle(title);
        book.setIsbn(isbn);
        book.setDescription("A " + genre.toLowerCase() + " book");
        book.setPublicationYear(year);
        book.setGenre(genre);
        book.setAvailableCopies(5);
        book.setTotalCopies(5);
        book.getAuthors().add(author);
        return bookRepository.save(book);
    }
}