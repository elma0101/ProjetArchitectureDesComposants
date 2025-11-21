package com.bookstore.service;

import com.bookstore.entity.*;
import com.bookstore.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service for seeding the database with test data for development and testing environments.
 * This service implements CommandLineRunner to execute seeding on application startup.
 */
@Service
@Profile({"dev", "test"})
public class DataSeedingService implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeedingService.class);

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    private LoanTrackingRepository loanTrackingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        logger.info("Starting data seeding for development/test environment");
        
        if (shouldSeedData()) {
            seedAuthors();
            seedBooks();
            seedUsers();
            seedLoans();
            seedRecommendations();
            seedLoanTracking();
            logger.info("Data seeding completed successfully");
        } else {
            logger.info("Data already exists, skipping seeding");
        }
    }

    private boolean shouldSeedData() {
        // Check if data already exists
        return authorRepository.count() == 0 && bookRepository.count() == 0;
    }

    private void seedAuthors() {
        logger.info("Seeding authors...");
        
        List<Author> authors = Arrays.asList(
            createAuthor("George", "Orwell", "English novelist and essayist, journalist and critic", 
                LocalDate.of(1903, 6, 25), "British"),
            createAuthor("Jane", "Austen", "English novelist known primarily for her six major novels", 
                LocalDate.of(1775, 12, 16), "British"),
            createAuthor("Harper", "Lee", "American novelist widely known for To Kill a Mockingbird", 
                LocalDate.of(1926, 4, 28), "American"),
            createAuthor("F. Scott", "Fitzgerald", "American novelist depicting the flamboyance and excess of the Jazz Age", 
                LocalDate.of(1896, 9, 24), "American"),
            createAuthor("J.K.", "Rowling", "British author best known for writing the Harry Potter fantasy series", 
                LocalDate.of(1965, 7, 31), "British"),
            createAuthor("Agatha", "Christie", "English writer known for her detective novels", 
                LocalDate.of(1890, 9, 15), "British"),
            createAuthor("Stephen", "King", "American author of horror, supernatural fiction, suspense", 
                LocalDate.of(1947, 9, 21), "American"),
            createAuthor("Gabriel", "García Márquez", "Colombian novelist, short-story writer, screenwriter", 
                LocalDate.of(1927, 3, 6), "Colombian")
        );
        
        authorRepository.saveAll(authors);
        logger.info("Seeded {} authors", authors.size());
    }

    private Author createAuthor(String firstName, String lastName, String biography, LocalDate birthDate, String nationality) {
        Author author = new Author();
        author.setFirstName(firstName);
        author.setLastName(lastName);
        author.setBiography(biography);
        author.setBirthDate(birthDate);
        author.setNationality(nationality);
        return author;
    }

    private void seedBooks() {
        logger.info("Seeding books...");
        
        List<Author> authors = authorRepository.findAll();
        
        List<Book> books = Arrays.asList(
            createBook("1984", "978-0-452-28423-4", "A dystopian social science fiction novel", 
                1949, "Dystopian Fiction", 3, 5, Set.of(authors.get(0))),
            createBook("Animal Farm", "978-0-452-28424-1", "An allegorical novella about farm animals", 
                1945, "Political Satire", 2, 3, Set.of(authors.get(0))),
            createBook("Pride and Prejudice", "978-0-14-143951-8", "A romantic novel of manners", 
                1813, "Romance", 4, 4, Set.of(authors.get(1))),
            createBook("Emma", "978-0-14-143952-5", "A novel about youthful hubris and romantic misunderstandings", 
                1815, "Romance", 2, 3, Set.of(authors.get(1))),
            createBook("To Kill a Mockingbird", "978-0-06-112008-4", "A novel about racial injustice", 
                1960, "Literary Fiction", 5, 6, Set.of(authors.get(2))),
            createBook("The Great Gatsby", "978-0-7432-7356-5", "A classic American novel about the Jazz Age", 
                1925, "Literary Fiction", 3, 4, Set.of(authors.get(3))),
            createBook("Harry Potter and the Philosopher's Stone", "978-0-7475-3269-9", "The first Harry Potter novel", 
                1997, "Fantasy", 0, 8, Set.of(authors.get(4))),
            createBook("Harry Potter and the Chamber of Secrets", "978-0-7475-3849-3", "The second Harry Potter novel", 
                1998, "Fantasy", 6, 6, Set.of(authors.get(4))),
            createBook("Murder on the Orient Express", "978-0-00-711926-0", "A detective novel featuring Hercule Poirot", 
                1934, "Mystery", 2, 3, Set.of(authors.get(5))),
            createBook("And Then There Were None", "978-0-00-711925-3", "A mystery novel about ten strangers", 
                1939, "Mystery", 1, 2, Set.of(authors.get(5))),
            createBook("The Shining", "978-0-385-12167-5", "A horror novel about a family at an isolated hotel", 
                1977, "Horror", 2, 3, Set.of(authors.get(6))),
            createBook("It", "978-0-670-81302-4", "A horror novel about children terrorized by a supernatural entity", 
                1986, "Horror", 1, 2, Set.of(authors.get(6))),
            createBook("One Hundred Years of Solitude", "978-0-06-088328-7", "A landmark novel about the Buendía family", 
                1967, "Magical Realism", 2, 2, Set.of(authors.get(7)))
        );
        
        bookRepository.saveAll(books);
        logger.info("Seeded {} books", books.size());
    }

    private Book createBook(String title, String isbn, String description, Integer publicationYear, 
                           String genre, Integer availableCopies, Integer totalCopies, Set<Author> authors) {
        Book book = new Book();
        book.setTitle(title);
        book.setIsbn(isbn);
        book.setDescription(description);
        book.setPublicationYear(publicationYear);
        book.setGenre(genre);
        book.setAvailableCopies(availableCopies);
        book.setTotalCopies(totalCopies);
        book.setAuthors(authors);
        return book;
    }

    private void seedUsers() {
        logger.info("Seeding users...");
        
        List<User> users = Arrays.asList(
            createUser("admin", "admin@bookstore.com", "Admin", "User", Set.of(Role.ADMIN)),
            createUser("librarian", "librarian@bookstore.com", "Library", "Staff", Set.of(Role.LIBRARIAN)),
            createUser("user1", "user1@bookstore.com", "John", "Smith", Set.of(Role.USER)),
            createUser("user2", "user2@bookstore.com", "Emily", "Johnson", Set.of(Role.USER))
        );
        
        userRepository.saveAll(users);
        logger.info("Seeded {} users", users.size());
    }

    private User createUser(String username, String email, String firstName, String lastName, Set<Role> roles) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRoles(roles);
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        return user;
    }

    private void seedLoans() {
        logger.info("Seeding loans...");
        
        List<Book> books = bookRepository.findAll();
        
        if (books.size() < 11) {
            logger.warn("Not enough books available for loan seeding. Expected at least 11, found {}", books.size());
            return;
        }
        
        List<Loan> loans = Arrays.asList(
            createLoan(books.get(0), "John Smith", "john.smith@email.com", "USER001", 
                LocalDate.of(2024, 1, 15), LocalDate.of(2024, 2, 15), LocalDate.of(2024, 2, 10), 
                LoanStatus.RETURNED, "Excellent condition upon return"),
            createLoan(books.get(2), "Emily Johnson", "emily.johnson@email.com", "USER002", 
                LocalDate.of(2024, 1, 20), LocalDate.of(2024, 2, 20), null, 
                LoanStatus.ACTIVE, "First-time borrower"),
            createLoan(books.get(4), "Michael Brown", "michael.brown@email.com", "USER003", 
                LocalDate.of(2024, 1, 10), LocalDate.of(2024, 2, 10), null, 
                LoanStatus.OVERDUE, "Reminder sent"),
            createLoan(books.get(5), "Sarah Davis", "sarah.davis@email.com", "USER004", 
                LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1), null, 
                LoanStatus.ACTIVE, "Regular borrower"),
            createLoan(books.get(8), "David Wilson", "david.wilson@email.com", "USER005", 
                LocalDate.of(2024, 1, 25), LocalDate.of(2024, 2, 25), LocalDate.of(2024, 2, 20), 
                LoanStatus.RETURNED, "Loved the book!"),
            createLoan(books.get(10), "Lisa Anderson", "lisa.anderson@email.com", "USER006", 
                LocalDate.of(2024, 2, 5), LocalDate.of(2024, 3, 5), null, 
                LoanStatus.ACTIVE, "Horror fan")
        );
        
        loanRepository.saveAll(loans);
        logger.info("Seeded {} loans", loans.size());
    }

    private Loan createLoan(Book book, String borrowerName, String borrowerEmail, String borrowerId,
                           LocalDate loanDate, LocalDate dueDate, LocalDate returnDate, 
                           LoanStatus status, String notes) {
        Loan loan = new Loan();
        loan.setBook(book);
        loan.setBorrowerName(borrowerName);
        loan.setBorrowerEmail(borrowerEmail);
        loan.setBorrowerId(borrowerId);
        loan.setLoanDate(loanDate);
        loan.setDueDate(dueDate);
        loan.setReturnDate(returnDate);
        loan.setStatus(status);
        loan.setNotes(notes);
        return loan;
    }

    private void seedRecommendations() {
        logger.info("Seeding recommendations...");
        
        List<Book> books = bookRepository.findAll();
        
        if (books.size() < 13) {
            logger.warn("Not enough books available for recommendation seeding. Expected at least 13, found {}", books.size());
            return;
        }
        
        List<Recommendation> recommendations = Arrays.asList(
            createRecommendation("USER001", books.get(1), 0.95, "Based on your interest in dystopian fiction", RecommendationType.CONTENT_BASED),
            createRecommendation("USER001", books.get(10), 0.75, "Popular among users with similar reading preferences", RecommendationType.COLLABORATIVE),
            createRecommendation("USER002", books.get(3), 0.90, "Another classic by Jane Austen", RecommendationType.CONTENT_BASED),
            createRecommendation("USER002", books.get(5), 0.80, "Highly rated literary fiction", RecommendationType.POPULAR),
            createRecommendation("USER003", books.get(12), 0.85, "Based on your interest in literary fiction", RecommendationType.CONTENT_BASED),
            createRecommendation("USER004", books.get(4), 0.92, "Trending among literature enthusiasts", RecommendationType.TRENDING),
            createRecommendation("USER005", books.get(9), 0.88, "Another mystery by Agatha Christie", RecommendationType.CONTENT_BASED),
            createRecommendation("USER006", books.get(11), 0.93, "Another horror novel by Stephen King", RecommendationType.CONTENT_BASED),
            createRecommendation("USER001", books.get(6), 0.70, "Popular fantasy series", RecommendationType.POPULAR),
            createRecommendation("USER002", books.get(7), 0.65, "Trending fantasy novel", RecommendationType.TRENDING)
        );
        
        recommendationRepository.saveAll(recommendations);
        logger.info("Seeded {} recommendations", recommendations.size());
    }

    private Recommendation createRecommendation(String userId, Book book, double score, String reason, RecommendationType type) {
        Recommendation recommendation = new Recommendation();
        recommendation.setUserId(userId);
        recommendation.setBook(book);
        recommendation.setScore(score);
        recommendation.setReason(reason);
        recommendation.setType(type);
        return recommendation;
    }

    private void seedLoanTracking() {
        logger.info("Seeding loan tracking...");
        
        List<Loan> loans = loanRepository.findAll();
        
        if (loans.size() < 3) {
            logger.warn("Not enough loans available for loan tracking seeding. Expected at least 3, found {}", loans.size());
            return;
        }
        
        List<LoanTracking> trackingEvents = Arrays.asList(
            createLoanTracking(loans.get(0), "LOAN_CREATED", "Loan created for borrower john.smith@email.com", LocalDateTime.of(2024, 1, 15, 10, 0)),
            createLoanTracking(loans.get(0), "NOTIFICATION_SENT", "LOAN_CONFIRMATION", LocalDateTime.of(2024, 1, 15, 10, 1)),
            createLoanTracking(loans.get(1), "LOAN_CREATED", "Loan created for borrower emily.johnson@email.com", LocalDateTime.of(2024, 1, 20, 14, 30)),
            createLoanTracking(loans.get(1), "NOTIFICATION_SENT", "LOAN_CONFIRMATION", LocalDateTime.of(2024, 1, 20, 14, 31)),
            createLoanTracking(loans.get(2), "LOAN_CREATED", "Loan created for borrower michael.brown@email.com", LocalDateTime.of(2024, 1, 10, 9, 15)),
            createLoanTracking(loans.get(2), "NOTIFICATION_SENT", "LOAN_CONFIRMATION", LocalDateTime.of(2024, 1, 10, 9, 16)),
            createLoanTracking(loans.get(2), "STATUS_CHANGE", "Status changed from ACTIVE to OVERDUE", LocalDateTime.of(2024, 2, 11, 9, 0)),
            createLoanTracking(loans.get(2), "NOTIFICATION_SENT", "OVERDUE_NOTIFICATION", LocalDateTime.of(2024, 2, 11, 9, 1)),
            createLoanTracking(loans.get(0), "STATUS_CHANGE", "Status changed from ACTIVE to RETURNED", LocalDateTime.of(2024, 2, 10, 16, 45)),
            createLoanTracking(loans.get(0), "LOAN_RETURNED", "Book returned on time", LocalDateTime.of(2024, 2, 10, 16, 45)),
            createLoanTracking(loans.get(0), "NOTIFICATION_SENT", "RETURN_CONFIRMATION", LocalDateTime.of(2024, 2, 10, 16, 46))
        );
        
        loanTrackingRepository.saveAll(trackingEvents);
        logger.info("Seeded {} loan tracking events", trackingEvents.size());
    }

    private LoanTracking createLoanTracking(Loan loan, String eventType, String description, LocalDateTime timestamp) {
        LoanTracking tracking = new LoanTracking();
        tracking.setLoanId(loan.getId());
        tracking.setEventType(eventType);
        tracking.setEventDescription(description);
        tracking.setEventTimestamp(timestamp);
        return tracking;
    }
}