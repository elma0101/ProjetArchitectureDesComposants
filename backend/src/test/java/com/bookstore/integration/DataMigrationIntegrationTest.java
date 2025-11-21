package com.bookstore.integration;

import com.bookstore.entity.*;
import com.bookstore.repository.*;
import com.bookstore.service.DataExportImportService;
import com.bookstore.service.DataSeedingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for data migration functionality including seeding, export, and import.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class DataMigrationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private DataSeedingService dataSeedingService;

    @Autowired
    private DataExportImportService dataExportImportService;

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

    @Test
    void shouldSeedDataSuccessfully() throws Exception {
        // Given - empty database
        assertThat(authorRepository.count()).isEqualTo(0);
        assertThat(bookRepository.count()).isEqualTo(0);

        // When
        dataSeedingService.run();

        // Then
        assertThat(authorRepository.count()).isGreaterThan(0);
        assertThat(bookRepository.count()).isGreaterThan(0);
        assertThat(userRepository.count()).isGreaterThan(0);
        assertThat(loanRepository.count()).isGreaterThan(0);
        assertThat(recommendationRepository.count()).isGreaterThan(0);

        // Verify data integrity
        List<Author> authors = authorRepository.findAll();
        assertThat(authors).isNotEmpty();
        assertThat(authors.get(0).getFirstName()).isNotNull();
        assertThat(authors.get(0).getLastName()).isNotNull();

        List<Book> books = bookRepository.findAll();
        assertThat(books).isNotEmpty();
        assertThat(books.get(0).getTitle()).isNotNull();
        assertThat(books.get(0).getIsbn()).isNotNull();
        assertThat(books.get(0).getAuthors()).isNotEmpty();

        List<User> users = userRepository.findAll();
        assertThat(users).isNotEmpty();
        assertThat(users.get(0).getUsername()).isNotNull();
        assertThat(users.get(0).getPassword()).isNotNull();
        assertThat(users.get(0).getPassword()).isNotEqualTo("password123"); // Should be encoded

        List<Loan> loans = loanRepository.findAll();
        assertThat(loans).isNotEmpty();
        assertThat(loans.get(0).getBook()).isNotNull();
        assertThat(loans.get(0).getBorrowerName()).isNotNull();

        List<Recommendation> recommendations = recommendationRepository.findAll();
        assertThat(recommendations).isNotEmpty();
        assertThat(recommendations.get(0).getBook()).isNotNull();
        assertThat(recommendations.get(0).getScore()).isNotNull();
    }

    @Test
    void shouldExportAndImportDataSuccessfully() throws Exception {
        // Given - seed some data first
        dataSeedingService.run();
        
        long originalAuthorCount = authorRepository.count();
        long originalBookCount = bookRepository.count();
        long originalUserCount = userRepository.count();
        long originalLoanCount = loanRepository.count();
        long originalRecommendationCount = recommendationRepository.count();

        // When - export data
        String exportPath = dataExportImportService.exportAllData();

        // Then - verify export file exists
        assertThat(Files.exists(Paths.get(exportPath))).isTrue();
        assertThat(exportPath).endsWith(".zip");

        // Clear database
        clearDatabase();
        assertThat(authorRepository.count()).isEqualTo(0);
        assertThat(bookRepository.count()).isEqualTo(0);

        // When - import data
        dataExportImportService.importAllData(exportPath);

        // Then - verify data is restored
        assertThat(authorRepository.count()).isEqualTo(originalAuthorCount);
        assertThat(bookRepository.count()).isEqualTo(originalBookCount);
        assertThat(userRepository.count()).isEqualTo(originalUserCount);
        assertThat(loanRepository.count()).isEqualTo(originalLoanCount);
        assertThat(recommendationRepository.count()).isEqualTo(originalRecommendationCount);

        // Verify data integrity after import
        List<Author> authors = authorRepository.findAll();
        assertThat(authors).isNotEmpty();
        assertThat(authors.get(0).getFirstName()).isNotNull();

        List<Book> books = bookRepository.findAll();
        assertThat(books).isNotEmpty();
        assertThat(books.get(0).getTitle()).isNotNull();
        assertThat(books.get(0).getAuthors()).isNotEmpty();

        // Clean up export file
        Files.deleteIfExists(Paths.get(exportPath));
    }

    @Test
    void shouldExportSpecificEntityData() throws Exception {
        // Given - seed some data
        dataSeedingService.run();

        // When - export authors only
        String exportPath = dataExportImportService.exportEntityData("authors");

        // Then
        assertThat(Files.exists(Paths.get(exportPath))).isTrue();
        assertThat(exportPath).contains("authors_export");
        assertThat(exportPath).endsWith(".json");

        // Verify file size is reasonable
        long fileSize = Files.size(Paths.get(exportPath));
        assertThat(fileSize).isGreaterThan(0);

        // Clean up
        Files.deleteIfExists(Paths.get(exportPath));
    }

    @Test
    void shouldGetExportStatistics() throws Exception {
        // Given - seed some data
        dataSeedingService.run();

        // When
        Map<String, Long> statistics = dataExportImportService.getExportStatistics();

        // Then
        assertThat(statistics).isNotEmpty();
        assertThat(statistics.get("authors")).isGreaterThan(0);
        assertThat(statistics.get("books")).isGreaterThan(0);
        assertThat(statistics.get("users")).isGreaterThan(0);
        assertThat(statistics.get("loans")).isGreaterThan(0);
        assertThat(statistics.get("recommendations")).isGreaterThan(0);
    }

    @Test
    void shouldValidateExportFiles() throws Exception {
        // Given - seed and export data
        dataSeedingService.run();
        String exportPath = dataExportImportService.exportAllData();

        // When & Then
        assertThat(dataExportImportService.validateExportFile(exportPath)).isTrue();
        assertThat(dataExportImportService.validateExportFile("nonexistent.zip")).isFalse();

        // Clean up
        Files.deleteIfExists(Paths.get(exportPath));
    }

    @Test
    void shouldHandleEmptyDatabaseExport() throws Exception {
        // Given - empty database
        clearDatabase();

        // When
        String exportPath = dataExportImportService.exportAllData();

        // Then
        assertThat(Files.exists(Paths.get(exportPath))).isTrue();
        
        Map<String, Long> statistics = dataExportImportService.getExportStatistics();
        assertThat(statistics.get("authors")).isEqualTo(0);
        assertThat(statistics.get("books")).isEqualTo(0);

        // Clean up
        Files.deleteIfExists(Paths.get(exportPath));
    }

    @Test
    void shouldPreserveDataRelationships() throws Exception {
        // Given - seed data
        dataSeedingService.run();

        // Find a book with authors
        Book originalBook = bookRepository.findAll().stream()
            .filter(book -> !book.getAuthors().isEmpty())
            .findFirst()
            .orElseThrow();

        int originalAuthorCount = originalBook.getAuthors().size();
        String originalTitle = originalBook.getTitle();

        // When - export and import
        String exportPath = dataExportImportService.exportAllData();
        clearDatabase();
        dataExportImportService.importAllData(exportPath);

        // Then - verify relationships are preserved
        Book importedBook = bookRepository.findByTitleContainingIgnoreCase(originalTitle, Pageable.unpaged())
            .getContent().stream()
            .filter(book -> book.getTitle().equals(originalTitle))
            .findFirst()
            .orElseThrow();
        assertThat(importedBook.getAuthors()).hasSize(originalAuthorCount);
        assertThat(importedBook.getAuthors()).isNotEmpty();

        // Verify author-book relationship works both ways
        Author author = importedBook.getAuthors().iterator().next();
        assertThat(author.getBooks()).contains(importedBook);

        // Clean up
        Files.deleteIfExists(Paths.get(exportPath));
    }

    @Test
    void shouldHandleLargeDataSets() throws Exception {
        // Given - seed data multiple times to create larger dataset
        dataSeedingService.run();
        
        // Add some additional test data
        createAdditionalTestData();

        long totalRecords = authorRepository.count() + bookRepository.count() + 
                           userRepository.count() + loanRepository.count() + 
                           recommendationRepository.count();

        // When - export large dataset
        String exportPath = dataExportImportService.exportAllData();

        // Then - verify export completed successfully
        assertThat(Files.exists(Paths.get(exportPath))).isTrue();
        
        long fileSize = Files.size(Paths.get(exportPath));
        assertThat(fileSize).isGreaterThan(1024); // At least 1KB

        // Verify import works with large dataset
        clearDatabase();
        dataExportImportService.importAllData(exportPath);

        long importedRecords = authorRepository.count() + bookRepository.count() + 
                              userRepository.count() + loanRepository.count() + 
                              recommendationRepository.count();
        
        assertThat(importedRecords).isEqualTo(totalRecords);

        // Clean up
        Files.deleteIfExists(Paths.get(exportPath));
    }

    private void clearDatabase() {
        loanTrackingRepository.deleteAll();
        recommendationRepository.deleteAll();
        loanRepository.deleteAll();
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        userRepository.deleteAll();
    }

    private void createAdditionalTestData() {
        // Create additional authors
        for (int i = 0; i < 5; i++) {
            Author author = new Author();
            author.setFirstName("Test" + i);
            author.setLastName("Author" + i);
            author.setBiography("Test biography " + i);
            authorRepository.save(author);
        }

        // Create additional books
        List<Author> authors = authorRepository.findAll();
        for (int i = 0; i < 10; i++) {
            Book book = new Book();
            book.setTitle("Test Book " + i);
            book.setIsbn("978-0-000-0000-" + String.format("%d", i));
            book.setDescription("Test description " + i);
            book.setPublicationYear(2000 + i);
            book.setGenre("Test Genre");
            book.setAvailableCopies(5);
            book.setTotalCopies(5);
            if (!authors.isEmpty()) {
                book.getAuthors().add(authors.get(i % authors.size()));
            }
            bookRepository.save(book);
        }
    }
}