package com.bookstore.integration;

import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import com.bookstore.entity.Loan;
import com.bookstore.entity.LoanStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive integration test suite that tests all major functionality
 */
@SpringBootTest
class ComprehensiveIntegrationTestSuite extends BaseIntegrationTest {

    @Autowired
    private TestDataManager testDataManager;

    private TestDataManager.TestDataSet testData;

    @BeforeEach
    void setUpTestData() {
        testDataManager.cleanupAll();
        testData = testDataManager.createCompleteTestDataSet();
    }

    @Test
    void shouldTestCompleteBookCRUDOperations() throws Exception {
        Book testBook = testData.getFirstBook();
        
        // Test GET all books
        mockMvc.perform(MockMvcRequestBuilders.get("/api/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.books").isArray())
                .andExpect(jsonPath("$.page.totalElements").value(4));

        // Test GET book by ID
        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/{id}", testBook.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(testBook.getTitle()))
                .andExpect(jsonPath("$.isbn").value(testBook.getIsbn()));

        // Test PATCH book
        String updateJson = """
                {
                    "description": "Updated description",
                    "genre": "Updated Genre"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/books/{id}", testBook.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.genre").value("Updated Genre"));

        // Test book search functionality
        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/search/findByTitleContainingIgnoreCase")
                        .param("title", "Mystery")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.books").isArray())
                .andExpect(jsonPath("$._embedded.books[0].title").value("Mystery Novel"));
    }

    @Test
    void shouldTestCompleteAuthorCRUDOperations() throws Exception {
        Author testAuthor = testData.getFirstAuthor();
        
        // Test GET all authors
        mockMvc.perform(MockMvcRequestBuilders.get("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.authors").isArray())
                .andExpect(jsonPath("$.page.totalElements").value(3));

        // Test GET author by ID
        mockMvc.perform(MockMvcRequestBuilders.get("/api/authors/{id}", testAuthor.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value(testAuthor.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(testAuthor.getLastName()));

        // Test author books relationship
        mockMvc.perform(MockMvcRequestBuilders.get("/api/authors/{id}/books", testAuthor.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.books").isArray());

        // Test author search functionality
        mockMvc.perform(MockMvcRequestBuilders.get("/api/authors/search/findByLastNameContainingIgnoreCase")
                        .param("lastName", "Doe")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.authors").isArray())
                .andExpect(jsonPath("$._embedded.authors[0].lastName").value("Doe"));
    }

    @Test
    void shouldTestCompleteLoanCRUDOperations() throws Exception {
        Loan testLoan = testData.getFirstLoan();
        
        // Test GET all loans
        mockMvc.perform(MockMvcRequestBuilders.get("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.loans").isArray())
                .andExpect(jsonPath("$.page.totalElements").value(4));

        // Test GET loan by ID
        mockMvc.perform(MockMvcRequestBuilders.get("/api/loans/{id}", testLoan.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.borrowerName").value(testLoan.getBorrowerName()))
                .andExpect(jsonPath("$.borrowerEmail").value(testLoan.getBorrowerEmail()));

        // Test loan search by status
        mockMvc.perform(MockMvcRequestBuilders.get("/api/loans/search/findByStatus")
                        .param("status", "ACTIVE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.loans").isArray());

        // Test overdue loans
        mockMvc.perform(MockMvcRequestBuilders.get("/api/loans/search/findByStatus")
                        .param("status", "OVERDUE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.loans").isArray())
                .andExpect(jsonPath("$._embedded.loans[0].status").value("OVERDUE"));

        // Test returned loans
        mockMvc.perform(MockMvcRequestBuilders.get("/api/loans/search/findByStatus")
                        .param("status", "RETURNED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.loans").isArray())
                .andExpect(jsonPath("$._embedded.loans[0].status").value("RETURNED"));
    }

    @Test
    void shouldTestHATEOASLinksForAllEntities() throws Exception {
        Book testBook = testData.getFirstBook();
        Author testAuthor = testData.getFirstAuthor();
        Loan testLoan = testData.getFirstLoan();

        // Test Book HATEOAS links
        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/{id}", testBook.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.book.href").exists())
                .andExpect(jsonPath("$._links.authors.href").exists());

        // Test Author HATEOAS links
        mockMvc.perform(MockMvcRequestBuilders.get("/api/authors/{id}", testAuthor.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.author.href").exists())
                .andExpect(jsonPath("$._links.books.href").exists());

        // Test Loan HATEOAS links
        mockMvc.perform(MockMvcRequestBuilders.get("/api/loans/{id}", testLoan.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.loan.href").exists())
                .andExpect(jsonPath("$._links.book.href").exists());
    }

    @Test
    void shouldTestPaginationForAllEntities() throws Exception {
        // Create additional test data for pagination
        testDataManager.createBulkTestData(10, 25, 30);

        // Test Book pagination
        mockMvc.perform(MockMvcRequestBuilders.get("/api/books")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "title,asc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.books.length()").value(10))
                .andExpect(jsonPath("$.page.size").value(10))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(29)) // 4 original + 25 bulk
                .andExpect(jsonPath("$.page.totalPages").value(3));

        // Test Author pagination
        mockMvc.perform(MockMvcRequestBuilders.get("/api/authors")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sort", "lastName,desc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.authors.length()").value(5))
                .andExpect(jsonPath("$.page.size").value(5))
                .andExpect(jsonPath("$.page.number").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(13)); // 3 original + 10 bulk

        // Test Loan pagination
        mockMvc.perform(MockMvcRequestBuilders.get("/api/loans")
                        .param("page", "0")
                        .param("size", "15")
                        .param("sort", "loanDate,desc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.loans.length()").value(15))
                .andExpect(jsonPath("$.page.size").value(15))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(34)); // 4 original + 30 bulk
    }

    @Test
    void shouldTestComplexRelationshipOperations() throws Exception {
        Book collaborativeBook = testData.getBook(3); // Book with multiple authors
        
        // Verify book has multiple authors
        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/{id}/authors", collaborativeBook.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.authors.length()").value(2));

        // Test adding a new author to the book
        Author newAuthor = testDataManager.createTestAuthor("New", "Author");
        String authorUri = getUrl("/api/authors/" + newAuthor.getId());

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/books/{id}/authors", collaborativeBook.getId())
                        .contentType("text/uri-list")
                        .content(authorUri))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Verify the new author was added
        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/{id}/authors", collaborativeBook.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.authors.length()").value(3));

        // Test removing an author from the book
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/books/{id}/authors/{authorId}", 
                        collaborativeBook.getId(), newAuthor.getId()))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Verify the author was removed
        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/{id}/authors", collaborativeBook.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.authors.length()").value(2));
    }

    @Test
    void shouldTestErrorHandlingScenarios() throws Exception {
        // Test 404 for non-existent book
        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/{id}", 99999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());

        // Test 404 for non-existent author
        mockMvc.perform(MockMvcRequestBuilders.get("/api/authors/{id}", 99999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());

        // Test 404 for non-existent loan
        mockMvc.perform(MockMvcRequestBuilders.get("/api/loans/{id}", 99999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());

        // Test validation error for invalid book creation
        String invalidBookJson = """
                {
                    "title": "",
                    "isbn": "invalid-isbn",
                    "totalCopies": -1
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBookJson))
                .andDo(print())
                .andExpect(status().isBadRequest());

        // Test validation error for invalid author creation
        String invalidAuthorJson = """
                {
                    "firstName": "",
                    "lastName": ""
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidAuthorJson))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldTestSearchFunctionalityAcrossAllEntities() throws Exception {
        // Test book search by various criteria
        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/search/findByGenreIgnoreCase")
                        .param("genre", "Fiction")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.books").isArray());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/search/findByPublicationYear")
                        .param("year", "2023")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.books").isArray());

        // Test author search by various criteria
        mockMvc.perform(MockMvcRequestBuilders.get("/api/authors/search/findByNationalityIgnoreCase")
                        .param("nationality", "American")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.authors").isArray());

        // Test loan search by various criteria
        mockMvc.perform(MockMvcRequestBuilders.get("/api/loans/search/findByBorrowerNameContainingIgnoreCase")
                        .param("borrowerName", "Alice")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.loans").isArray());
    }

    @Test
    void shouldTestDataConsistencyAcrossOperations() throws Exception {
        Book testBook = testData.getFirstBook();
        int initialAvailableCopies = testBook.getAvailableCopies();

        // Create a new loan
        String loanJson = String.format("""
                {
                    "borrowerName": "Consistency Tester",
                    "borrowerEmail": "tester@example.com",
                    "borrowerId": "TESTER001",
                    "loanDate": "%s",
                    "dueDate": "%s",
                    "status": "ACTIVE",
                    "book": "/api/books/%d"
                }
                """, 
                java.time.LocalDate.now().toString(),
                java.time.LocalDate.now().plusDays(14).toString(),
                testBook.getId());

        String loanResponse = mockMvc.perform(MockMvcRequestBuilders.post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loanJson))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Loan createdLoan = fromJson(loanResponse, Loan.class);

        // Verify book availability decreased
        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/{id}", testBook.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCopies").value(initialAvailableCopies - 1));

        // Return the loan
        String returnJson = String.format("""
                {
                    "status": "RETURNED",
                    "returnDate": "%s"
                }
                """, java.time.LocalDate.now().toString());

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/loans/{id}", createdLoan.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(returnJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"));

        // Verify book availability increased back
        mockMvc.perform(MockMvcRequestBuilders.get("/api/books/{id}", testBook.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCopies").value(initialAvailableCopies));
    }
}