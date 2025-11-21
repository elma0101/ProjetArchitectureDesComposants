package com.bookstore.integration;

import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import com.bookstore.entity.Loan;
import com.bookstore.entity.LoanStatus;
import com.bookstore.repository.AuthorRepository;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Utility class for managing test data creation and cleanup
 */
@Component
@Transactional
public class TestDataManager {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private LoanRepository loanRepository;

    /**
     * Clean up all test data
     */
    public void cleanupAll() {
        loanRepository.deleteAll();
        bookRepository.deleteAll();
        authorRepository.deleteAll();
    }

    /**
     * Create a test author with default values
     */
    public Author createTestAuthor(String firstName, String lastName) {
        Author author = new Author();
        author.setFirstName(firstName);
        author.setLastName(lastName);
        author.setBiography("Test author biography for " + firstName + " " + lastName);
        author.setBirthDate(LocalDate.of(1980, 1, 1));
        author.setNationality("American");
        return authorRepository.save(author);
    }

    /**
     * Create a test author with custom values
     */
    public Author createTestAuthor(String firstName, String lastName, String biography, 
                                 LocalDate birthDate, String nationality) {
        Author author = new Author();
        author.setFirstName(firstName);
        author.setLastName(lastName);
        author.setBiography(biography);
        author.setBirthDate(birthDate);
        author.setNationality(nationality);
        return authorRepository.save(author);
    }

    /**
     * Create a test book with default values
     */
    public Book createTestBook(String title, String isbn) {
        Book book = new Book();
        book.setTitle(title);
        book.setIsbn(isbn);
        book.setDescription("Test book description for " + title);
        book.setPublicationYear(2023);
        book.setGenre("Fiction");
        book.setAvailableCopies(5);
        book.setTotalCopies(5);
        return bookRepository.save(book);
    }

    /**
     * Create a test book with custom values
     */
    public Book createTestBook(String title, String isbn, String description, 
                             Integer publicationYear, String genre, 
                             Integer availableCopies, Integer totalCopies) {
        Book book = new Book();
        book.setTitle(title);
        book.setIsbn(isbn);
        book.setDescription(description);
        book.setPublicationYear(publicationYear);
        book.setGenre(genre);
        book.setAvailableCopies(availableCopies);
        book.setTotalCopies(totalCopies);
        return bookRepository.save(book);
    }

    /**
     * Create a test book with authors
     */
    public Book createTestBookWithAuthors(String title, String isbn, Set<Author> authors) {
        Book book = createTestBook(title, isbn);
        book.setAuthors(authors);
        return bookRepository.save(book);
    }

    /**
     * Create a test loan with default values
     */
    public Loan createTestLoan(Book book, String borrowerName, String borrowerEmail) {
        Loan loan = new Loan();
        loan.setBook(book);
        loan.setBorrowerName(borrowerName);
        loan.setBorrowerEmail(borrowerEmail);
        loan.setBorrowerId("TEST" + System.currentTimeMillis());
        loan.setLoanDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(14));
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setNotes("Test loan");
        return loanRepository.save(loan);
    }

    /**
     * Create a test loan with custom values
     */
    public Loan createTestLoan(Book book, String borrowerName, String borrowerEmail, 
                             String borrowerId, LocalDate loanDate, LocalDate dueDate, 
                             LoanStatus status, String notes) {
        Loan loan = new Loan();
        loan.setBook(book);
        loan.setBorrowerName(borrowerName);
        loan.setBorrowerEmail(borrowerEmail);
        loan.setBorrowerId(borrowerId);
        loan.setLoanDate(loanDate);
        loan.setDueDate(dueDate);
        loan.setStatus(status);
        loan.setNotes(notes);
        return loanRepository.save(loan);
    }

    /**
     * Create an overdue loan
     */
    public Loan createOverdueLoan(Book book, String borrowerName, String borrowerEmail) {
        return createTestLoan(book, borrowerName, borrowerEmail, 
                            "OVERDUE" + System.currentTimeMillis(),
                            LocalDate.now().minusDays(20),
                            LocalDate.now().minusDays(5),
                            LoanStatus.OVERDUE,
                            "Overdue test loan");
    }

    /**
     * Create a returned loan
     */
    public Loan createReturnedLoan(Book book, String borrowerName, String borrowerEmail) {
        Loan loan = createTestLoan(book, borrowerName, borrowerEmail, 
                                 "RETURNED" + System.currentTimeMillis(),
                                 LocalDate.now().minusDays(10),
                                 LocalDate.now().plusDays(4),
                                 LoanStatus.RETURNED,
                                 "Returned test loan");
        loan.setReturnDate(LocalDate.now().minusDays(1));
        return loanRepository.save(loan);
    }

    /**
     * Create a complete dataset for testing
     */
    public TestDataSet createCompleteTestDataSet() {
        // Create authors
        Author author1 = createTestAuthor("John", "Doe", "Mystery writer", 
                                        LocalDate.of(1975, 3, 15), "American");
        Author author2 = createTestAuthor("Jane", "Smith", "Science fiction author", 
                                        LocalDate.of(1980, 7, 22), "British");
        Author author3 = createTestAuthor("Bob", "Johnson", "Fantasy author", 
                                        LocalDate.of(1970, 11, 8), "Canadian");

        // Create books
        Book book1 = createTestBookWithAuthors("Mystery Novel", "978-1111111111", Set.of(author1));
        Book book2 = createTestBookWithAuthors("Sci-Fi Adventure", "978-2222222222", Set.of(author2));
        Book book3 = createTestBookWithAuthors("Fantasy Epic", "978-3333333333", Set.of(author3));
        Book book4 = createTestBookWithAuthors("Collaborative Work", "978-4444444444", Set.of(author1, author2));

        // Create loans
        Loan activeLoan1 = createTestLoan(book1, "Alice Reader", "alice@example.com");
        Loan activeLoan2 = createTestLoan(book2, "Bob Reader", "bob@example.com");
        Loan overdueLoan = createOverdueLoan(book3, "Charlie Reader", "charlie@example.com");
        Loan returnedLoan = createReturnedLoan(book4, "Diana Reader", "diana@example.com");

        return new TestDataSet(
                List.of(author1, author2, author3),
                List.of(book1, book2, book3, book4),
                List.of(activeLoan1, activeLoan2, overdueLoan, returnedLoan)
        );
    }

    /**
     * Create bulk test data for performance testing
     */
    public TestDataSet createBulkTestData(int authorCount, int bookCount, int loanCount) {
        List<Author> authors = new ArrayList<>();
        List<Book> books = new ArrayList<>();
        List<Loan> loans = new ArrayList<>();

        // Create authors
        for (int i = 1; i <= authorCount; i++) {
            Author author = createTestAuthor("Author" + i, "LastName" + i, 
                                           "Biography for author " + i,
                                           LocalDate.of(1950 + (i % 50), (i % 12) + 1, (i % 28) + 1),
                                           "Country" + (i % 10));
            authors.add(author);
        }

        // Create books
        for (int i = 1; i <= bookCount; i++) {
            Author randomAuthor = authors.get((i - 1) % authors.size());
            Book book = createTestBookWithAuthors("Book Title " + i, 
                                                "978-" + String.format("%010d", i),
                                                Set.of(randomAuthor));
            book.setGenre("Genre" + (i % 5));
            book.setPublicationYear(1990 + (i % 30));
            book = bookRepository.save(book);
            books.add(book);
        }

        // Create loans
        for (int i = 1; i <= loanCount; i++) {
            Book randomBook = books.get((i - 1) % books.size());
            LoanStatus status = switch (i % 4) {
                case 0 -> LoanStatus.ACTIVE;
                case 1 -> LoanStatus.RETURNED;
                case 2 -> LoanStatus.OVERDUE;
                default -> LoanStatus.ACTIVE;
            };
            
            Loan loan = createTestLoan(randomBook, "Borrower" + i, "borrower" + i + "@example.com",
                                     "BORROWER" + String.format("%06d", i),
                                     LocalDate.now().minusDays(i % 30),
                                     LocalDate.now().plusDays(14 - (i % 30)),
                                     status,
                                     "Bulk test loan " + i);
            
            if (status == LoanStatus.RETURNED) {
                loan.setReturnDate(LocalDate.now().minusDays((i % 10) + 1));
                loan = loanRepository.save(loan);
            }
            
            loans.add(loan);
        }

        return new TestDataSet(authors, books, loans);
    }

    /**
     * Data class to hold test data sets
     */
    public static class TestDataSet {
        private final List<Author> authors;
        private final List<Book> books;
        private final List<Loan> loans;

        public TestDataSet(List<Author> authors, List<Book> books, List<Loan> loans) {
            this.authors = authors;
            this.books = books;
            this.loans = loans;
        }

        public List<Author> getAuthors() { return authors; }
        public List<Book> getBooks() { return books; }
        public List<Loan> getLoans() { return loans; }

        public Author getFirstAuthor() { return authors.isEmpty() ? null : authors.get(0); }
        public Book getFirstBook() { return books.isEmpty() ? null : books.get(0); }
        public Loan getFirstLoan() { return loans.isEmpty() ? null : loans.get(0); }

        public Author getAuthor(int index) { 
            return index < authors.size() ? authors.get(index) : null; 
        }
        public Book getBook(int index) { 
            return index < books.size() ? books.get(index) : null; 
        }
        public Loan getLoan(int index) { 
            return index < loans.size() ? loans.get(index) : null; 
        }
    }
}