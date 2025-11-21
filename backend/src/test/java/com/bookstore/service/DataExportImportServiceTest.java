package com.bookstore.service;

import com.bookstore.entity.*;
import com.bookstore.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataExportImportServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private RecommendationRepository recommendationRepository;

    @Mock
    private LoanTrackingRepository loanTrackingRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private DataExportImportService dataExportImportService;

    @TempDir
    Path tempDir;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        ReflectionTestUtils.setField(dataExportImportService, "objectMapper", objectMapper);
    }

    @Test
    void shouldExportAllDataToZipFile() throws IOException {
        // Given
        when(authorRepository.findAll()).thenReturn(createMockAuthors());
        when(bookRepository.findAll()).thenReturn(createMockBooks());
        when(userRepository.findAll()).thenReturn(createMockUsers());
        when(loanRepository.findAll()).thenReturn(createMockLoans());
        when(recommendationRepository.findAll()).thenReturn(createMockRecommendations());
        when(loanTrackingRepository.findAll()).thenReturn(createMockLoanTracking());
        when(auditLogRepository.findAll()).thenReturn(createMockAuditLogs());

        when(authorRepository.count()).thenReturn(2L);
        when(bookRepository.count()).thenReturn(2L);
        when(userRepository.count()).thenReturn(2L);
        when(loanRepository.count()).thenReturn(1L);
        when(recommendationRepository.count()).thenReturn(1L);
        when(loanTrackingRepository.count()).thenReturn(1L);
        when(auditLogRepository.count()).thenReturn(1L);

        // When
        String exportPath = dataExportImportService.exportAllData();

        // Then
        assertThat(exportPath).isNotNull();
        assertThat(Files.exists(Path.of(exportPath))).isTrue();
        assertThat(exportPath).endsWith(".zip");

        // Verify ZIP file contains expected entries
        try (ZipFile zipFile = new ZipFile(exportPath)) {
            assertThat(zipFile.getEntry("authors.json")).isNotNull();
            assertThat(zipFile.getEntry("books.json")).isNotNull();
            assertThat(zipFile.getEntry("users.json")).isNotNull();
            assertThat(zipFile.getEntry("loans.json")).isNotNull();
            assertThat(zipFile.getEntry("recommendations.json")).isNotNull();
            assertThat(zipFile.getEntry("loan_tracking.json")).isNotNull();
            assertThat(zipFile.getEntry("audit_logs.json")).isNotNull();
            assertThat(zipFile.getEntry("metadata.json")).isNotNull();
        }
    }

    @Test
    void shouldExportSpecificEntityData() throws IOException {
        // Given
        when(authorRepository.findAll()).thenReturn(createMockAuthors());

        // When
        String exportPath = dataExportImportService.exportEntityData("authors");

        // Then
        assertThat(exportPath).isNotNull();
        assertThat(Files.exists(Path.of(exportPath))).isTrue();
        assertThat(exportPath).contains("authors_export");
        assertThat(exportPath).endsWith(".json");

        // Verify JSON content
        List<Author> exportedAuthors = objectMapper.readValue(
            Files.readAllBytes(Path.of(exportPath)),
            objectMapper.getTypeFactory().constructCollectionType(List.class, Author.class)
        );
        assertThat(exportedAuthors).hasSize(2);
    }

    @Test
    void shouldThrowExceptionForUnknownEntityType() {
        // When & Then
        assertThatThrownBy(() -> dataExportImportService.exportEntityData("unknown"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unknown entity type");
    }

    @Test
    void shouldImportAllDataFromZipFile() throws IOException {
        // Given
        String exportPath = createTestExportFile();

        // When
        dataExportImportService.importAllData(exportPath);

        // Then
        verify(authorRepository).saveAll(anyList());
        verify(bookRepository).saveAll(anyList());
        verify(userRepository).saveAll(anyList());
        verify(loanRepository).saveAll(anyList());
        verify(recommendationRepository).saveAll(anyList());
        verify(loanTrackingRepository).saveAll(anyList());
        verify(auditLogRepository).saveAll(anyList());
    }

    @Test
    void shouldImportSpecificEntityData() throws IOException {
        // Given
        List<Author> authors = createMockAuthors();
        Path jsonFile = tempDir.resolve("authors.json");
        objectMapper.writeValue(jsonFile.toFile(), authors);

        // When
        dataExportImportService.importEntityData("authors", jsonFile.toString());

        // Then
        verify(authorRepository).saveAll(anyList());
    }

    @Test
    void shouldThrowExceptionWhenImportFileNotFound() {
        // When & Then
        assertThatThrownBy(() -> dataExportImportService.importAllData("nonexistent.zip"))
            .isInstanceOf(IOException.class);
    }

    @Test
    void shouldRedactPasswordsInUserExport() throws IOException {
        // Given
        List<User> users = createMockUsers();
        users.get(0).setPassword("secret_password");
        when(userRepository.findAll()).thenReturn(users);

        // When
        String exportPath = dataExportImportService.exportEntityData("users");

        // Then
        List<User> exportedUsers = objectMapper.readValue(
            Files.readAllBytes(Path.of(exportPath)),
            objectMapper.getTypeFactory().constructCollectionType(List.class, User.class)
        );
        assertThat(exportedUsers.get(0).getPassword()).isEqualTo("[REDACTED]");
    }

    @Test
    void shouldSkipUsersWithRedactedPasswordsOnImport() throws IOException {
        // Given
        List<User> users = createMockUsers();
        users.get(0).setPassword("[REDACTED]");
        Path jsonFile = tempDir.resolve("users.json");
        objectMapper.writeValue(jsonFile.toFile(), users);

        // When
        dataExportImportService.importEntityData("users", jsonFile.toString());

        // Then
        verify(userRepository).saveAll(argThat(userList -> 
            ((List<User>) userList).stream().noneMatch(user -> "[REDACTED]".equals(user.getPassword()))
        ));
    }

    @Test
    void shouldGetExportStatistics() {
        // Given
        when(authorRepository.count()).thenReturn(5L);
        when(bookRepository.count()).thenReturn(10L);
        when(userRepository.count()).thenReturn(3L);
        when(loanRepository.count()).thenReturn(7L);
        when(recommendationRepository.count()).thenReturn(15L);
        when(loanTrackingRepository.count()).thenReturn(20L);
        when(auditLogRepository.count()).thenReturn(100L);

        // When
        Map<String, Long> statistics = dataExportImportService.getExportStatistics();

        // Then
        assertThat(statistics).containsEntry("authors", 5L);
        assertThat(statistics).containsEntry("books", 10L);
        assertThat(statistics).containsEntry("users", 3L);
        assertThat(statistics).containsEntry("loans", 7L);
        assertThat(statistics).containsEntry("recommendations", 15L);
        assertThat(statistics).containsEntry("loanTracking", 20L);
        assertThat(statistics).containsEntry("auditLogs", 100L);
    }

    @Test
    void shouldValidateValidZipFile() throws IOException {
        // Given
        String exportPath = createTestExportFile();

        // When
        boolean isValid = dataExportImportService.validateExportFile(exportPath);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldValidateValidJsonFile() throws IOException {
        // Given
        List<Author> authors = createMockAuthors();
        Path jsonFile = tempDir.resolve("authors.json");
        objectMapper.writeValue(jsonFile.toFile(), authors);

        // When
        boolean isValid = dataExportImportService.validateExportFile(jsonFile.toString());

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldReturnFalseForNonexistentFile() {
        // When
        boolean isValid = dataExportImportService.validateExportFile("nonexistent.zip");

        // Then
        assertThat(isValid).isFalse();
    }

    private String createTestExportFile() throws IOException {
        // Mock repository data
        when(authorRepository.findAll()).thenReturn(createMockAuthors());
        when(bookRepository.findAll()).thenReturn(createMockBooks());
        when(userRepository.findAll()).thenReturn(createMockUsers());
        when(loanRepository.findAll()).thenReturn(createMockLoans());
        when(recommendationRepository.findAll()).thenReturn(createMockRecommendations());
        when(loanTrackingRepository.findAll()).thenReturn(createMockLoanTracking());
        when(auditLogRepository.findAll()).thenReturn(createMockAuditLogs());

        when(authorRepository.count()).thenReturn(2L);
        when(bookRepository.count()).thenReturn(2L);
        when(userRepository.count()).thenReturn(2L);
        when(loanRepository.count()).thenReturn(1L);
        when(recommendationRepository.count()).thenReturn(1L);
        when(loanTrackingRepository.count()).thenReturn(1L);
        when(auditLogRepository.count()).thenReturn(1L);

        return dataExportImportService.exportAllData();
    }

    private List<Author> createMockAuthors() {
        Author author1 = new Author();
        author1.setId(1L);
        author1.setFirstName("George");
        author1.setLastName("Orwell");
        author1.setBirthDate(LocalDate.of(1903, 6, 25));

        Author author2 = new Author();
        author2.setId(2L);
        author2.setFirstName("Jane");
        author2.setLastName("Austen");
        author2.setBirthDate(LocalDate.of(1775, 12, 16));

        return Arrays.asList(author1, author2);
    }

    private List<Book> createMockBooks() {
        Book book1 = new Book();
        book1.setId(1L);
        book1.setTitle("1984");
        book1.setIsbn("978-0-452-28423-4");
        book1.setPublicationYear(1949);

        Book book2 = new Book();
        book2.setId(2L);
        book2.setTitle("Pride and Prejudice");
        book2.setIsbn("978-0-14-143951-8");
        book2.setPublicationYear(1813);

        return Arrays.asList(book1, book2);
    }

    private List<User> createMockUsers() {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("admin");
        user1.setEmail("admin@bookstore.com");
        user1.setPassword("encoded_password");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user1");
        user2.setEmail("user1@bookstore.com");
        user2.setPassword("encoded_password");

        return Arrays.asList(user1, user2);
    }

    private List<Loan> createMockLoans() {
        Loan loan = new Loan();
        loan.setId(1L);
        loan.setBorrowerName("John Smith");
        loan.setBorrowerEmail("john@example.com");
        loan.setLoanDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(30));
        loan.setStatus(LoanStatus.ACTIVE);

        return Arrays.asList(loan);
    }

    private List<Recommendation> createMockRecommendations() {
        Recommendation recommendation = new Recommendation();
        recommendation.setId(1L);
        recommendation.setUserId("USER001");
        recommendation.setScore(0.95);
        recommendation.setType(RecommendationType.CONTENT_BASED);

        return Arrays.asList(recommendation);
    }

    private List<LoanTracking> createMockLoanTracking() {
        LoanTracking tracking = new LoanTracking();
        tracking.setId(1L);
        tracking.setEventType("LOAN_CREATED");
        tracking.setEventDescription("Loan created");
        tracking.setEventTimestamp(LocalDateTime.now());

        return Arrays.asList(tracking);
    }

    private List<AuditLog> createMockAuditLogs() {
        AuditLog auditLog = new AuditLog();
        auditLog.setId(1L);
        auditLog.setResourceType("Book");
        auditLog.setResourceId("1");
        auditLog.setAction("CREATE");
        auditLog.setTimestamp(LocalDateTime.now());

        return Arrays.asList(auditLog);
    }
}