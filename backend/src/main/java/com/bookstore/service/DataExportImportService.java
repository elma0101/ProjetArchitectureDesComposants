package com.bookstore.service;

import com.bookstore.entity.*;
import com.bookstore.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Service for exporting and importing bookstore data.
 * Supports JSON format with compression for efficient data transfer.
 */
@Service
public class DataExportImportService {

    private static final Logger logger = LoggerFactory.getLogger(DataExportImportService.class);
    private static final String EXPORT_DIR = "exports";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

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
    private AuditLogRepository auditLogRepository;

    private final ObjectMapper objectMapper;

    public DataExportImportService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Export all data to a compressed ZIP file
     */
    @Transactional(readOnly = true)
    public String exportAllData() throws IOException {
        logger.info("Starting full data export");
        
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String exportFileName = String.format("bookstore_export_%s.zip", timestamp);
        Path exportPath = createExportDirectory().resolve(exportFileName);

        try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(exportPath))) {
            exportAuthors(zipOut);
            exportBooks(zipOut);
            exportUsers(zipOut);
            exportLoans(zipOut);
            exportRecommendations(zipOut);
            exportLoanTracking(zipOut);
            exportAuditLogs(zipOut);
            exportMetadata(zipOut, timestamp);
        }

        logger.info("Data export completed: {}", exportPath.toString());
        return exportPath.toString();
    }

    /**
     * Export specific entity type to a JSON file
     */
    @Transactional(readOnly = true)
    public String exportEntityData(String entityType) throws IOException {
        logger.info("Starting export for entity type: {}", entityType);
        
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String exportFileName = String.format("%s_export_%s.json", entityType.toLowerCase(), timestamp);
        Path exportPath = createExportDirectory().resolve(exportFileName);

        Object data = getEntityData(entityType);
        objectMapper.writeValue(exportPath.toFile(), data);

        logger.info("Entity export completed: {}", exportPath.toString());
        return exportPath.toString();
    }

    /**
     * Import data from a ZIP file
     */
    @Transactional
    public void importAllData(String filePath) throws IOException {
        logger.info("Starting data import from: {}", filePath);
        
        Path importPath = Paths.get(filePath);
        if (!Files.exists(importPath)) {
            throw new FileNotFoundException("Import file not found: " + filePath);
        }

        try (ZipInputStream zipIn = new ZipInputStream(Files.newInputStream(importPath))) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    importEntityFromZip(entry.getName(), zipIn);
                }
                zipIn.closeEntry();
            }
        }

        logger.info("Data import completed successfully");
    }

    /**
     * Import specific entity data from a JSON file
     */
    @Transactional
    public void importEntityData(String entityType, String filePath) throws IOException {
        logger.info("Starting import for entity type: {} from: {}", entityType, filePath);
        
        Path importPath = Paths.get(filePath);
        if (!Files.exists(importPath)) {
            throw new FileNotFoundException("Import file not found: " + filePath);
        }

        byte[] jsonData = Files.readAllBytes(importPath);
        importEntityData(entityType, jsonData);

        logger.info("Entity import completed for: {}", entityType);
    }

    private Path createExportDirectory() throws IOException {
        Path exportDir = Paths.get(EXPORT_DIR);
        if (!Files.exists(exportDir)) {
            Files.createDirectories(exportDir);
        }
        return exportDir;
    }

    private void exportAuthors(ZipOutputStream zipOut) throws IOException {
        logger.debug("Exporting authors");
        List<Author> authors = authorRepository.findAll();
        addZipEntry(zipOut, "authors.json", objectMapper.writeValueAsBytes(authors));
    }

    private void exportBooks(ZipOutputStream zipOut) throws IOException {
        logger.debug("Exporting books");
        List<Book> books = bookRepository.findAll();
        addZipEntry(zipOut, "books.json", objectMapper.writeValueAsBytes(books));
    }

    private void exportUsers(ZipOutputStream zipOut) throws IOException {
        logger.debug("Exporting users");
        List<User> users = userRepository.findAll();
        // Remove sensitive password information for export
        users.forEach(user -> user.setPassword("[REDACTED]"));
        addZipEntry(zipOut, "users.json", objectMapper.writeValueAsBytes(users));
    }

    private void exportLoans(ZipOutputStream zipOut) throws IOException {
        logger.debug("Exporting loans");
        List<Loan> loans = loanRepository.findAll();
        addZipEntry(zipOut, "loans.json", objectMapper.writeValueAsBytes(loans));
    }

    private void exportRecommendations(ZipOutputStream zipOut) throws IOException {
        logger.debug("Exporting recommendations");
        List<Recommendation> recommendations = recommendationRepository.findAll();
        addZipEntry(zipOut, "recommendations.json", objectMapper.writeValueAsBytes(recommendations));
    }

    private void exportLoanTracking(ZipOutputStream zipOut) throws IOException {
        logger.debug("Exporting loan tracking");
        List<LoanTracking> loanTracking = loanTrackingRepository.findAll();
        addZipEntry(zipOut, "loan_tracking.json", objectMapper.writeValueAsBytes(loanTracking));
    }

    private void exportAuditLogs(ZipOutputStream zipOut) throws IOException {
        logger.debug("Exporting audit logs");
        List<AuditLog> auditLogs = auditLogRepository.findAll();
        addZipEntry(zipOut, "audit_logs.json", objectMapper.writeValueAsBytes(auditLogs));
    }

    private void exportMetadata(ZipOutputStream zipOut, String timestamp) throws IOException {
        logger.debug("Exporting metadata");
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("exportTimestamp", timestamp);
        metadata.put("exportVersion", "1.0");
        metadata.put("authorCount", authorRepository.count());
        metadata.put("bookCount", bookRepository.count());
        metadata.put("userCount", userRepository.count());
        metadata.put("loanCount", loanRepository.count());
        metadata.put("recommendationCount", recommendationRepository.count());
        metadata.put("loanTrackingCount", loanTrackingRepository.count());
        metadata.put("auditLogCount", auditLogRepository.count());
        
        addZipEntry(zipOut, "metadata.json", objectMapper.writeValueAsBytes(metadata));
    }

    private void addZipEntry(ZipOutputStream zipOut, String fileName, byte[] data) throws IOException {
        ZipEntry entry = new ZipEntry(fileName);
        zipOut.putNextEntry(entry);
        zipOut.write(data);
        zipOut.closeEntry();
    }

    private Object getEntityData(String entityType) {
        return switch (entityType.toLowerCase()) {
            case "authors" -> authorRepository.findAll();
            case "books" -> bookRepository.findAll();
            case "users" -> {
                List<User> users = userRepository.findAll();
                users.forEach(user -> user.setPassword("[REDACTED]"));
                yield users;
            }
            case "loans" -> loanRepository.findAll();
            case "recommendations" -> recommendationRepository.findAll();
            case "loantracking" -> loanTrackingRepository.findAll();
            case "auditlogs" -> auditLogRepository.findAll();
            default -> throw new IllegalArgumentException("Unknown entity type: " + entityType);
        };
    }

    private void importEntityFromZip(String fileName, ZipInputStream zipIn) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int bytesRead;
        while ((bytesRead = zipIn.read(data)) != -1) {
            buffer.write(data, 0, bytesRead);
        }

        String entityType = fileName.replace(".json", "").replace("_", "");
        importEntityData(entityType, buffer.toByteArray());
    }

    private void importEntityData(String entityType, byte[] jsonData) throws IOException {
        switch (entityType.toLowerCase()) {
            case "authors" -> {
                List<Author> authors = objectMapper.readValue(jsonData, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Author.class));
                authorRepository.saveAll(authors);
                logger.debug("Imported {} authors", authors.size());
            }
            case "books" -> {
                List<Book> books = objectMapper.readValue(jsonData, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Book.class));
                bookRepository.saveAll(books);
                logger.debug("Imported {} books", books.size());
            }
            case "users" -> {
                List<User> users = objectMapper.readValue(jsonData, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, User.class));
                // Skip users with redacted passwords
                users.removeIf(user -> "[REDACTED]".equals(user.getPassword()));
                userRepository.saveAll(users);
                logger.debug("Imported {} users", users.size());
            }
            case "loans" -> {
                List<Loan> loans = objectMapper.readValue(jsonData, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Loan.class));
                loanRepository.saveAll(loans);
                logger.debug("Imported {} loans", loans.size());
            }
            case "recommendations" -> {
                List<Recommendation> recommendations = objectMapper.readValue(jsonData, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Recommendation.class));
                recommendationRepository.saveAll(recommendations);
                logger.debug("Imported {} recommendations", recommendations.size());
            }
            case "loantracking" -> {
                List<LoanTracking> loanTracking = objectMapper.readValue(jsonData, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, LoanTracking.class));
                loanTrackingRepository.saveAll(loanTracking);
                logger.debug("Imported {} loan tracking events", loanTracking.size());
            }
            case "auditlogs" -> {
                List<AuditLog> auditLogs = objectMapper.readValue(jsonData, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, AuditLog.class));
                auditLogRepository.saveAll(auditLogs);
                logger.debug("Imported {} audit logs", auditLogs.size());
            }
            case "metadata" -> {
                // Metadata is informational only, no import needed
                logger.debug("Skipping metadata import");
            }
            default -> logger.warn("Unknown entity type for import: {}", entityType);
        }
    }

    /**
     * Get export statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getExportStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("authors", authorRepository.count());
        stats.put("books", bookRepository.count());
        stats.put("users", userRepository.count());
        stats.put("loans", loanRepository.count());
        stats.put("recommendations", recommendationRepository.count());
        stats.put("loanTracking", loanTrackingRepository.count());
        stats.put("auditLogs", auditLogRepository.count());
        return stats;
    }

    /**
     * Validate export file integrity
     */
    public boolean validateExportFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                return false;
            }

            if (filePath.endsWith(".zip")) {
                return validateZipFile(path);
            } else if (filePath.endsWith(".json")) {
                return validateJsonFile(path);
            }

            return false;
        } catch (Exception e) {
            logger.error("Error validating export file: {}", filePath, e);
            return false;
        }
    }

    private boolean validateZipFile(Path path) throws IOException {
        try (ZipInputStream zipIn = new ZipInputStream(Files.newInputStream(path))) {
            ZipEntry entry;
            boolean hasMetadata = false;
            while ((entry = zipIn.getNextEntry()) != null) {
                if ("metadata.json".equals(entry.getName())) {
                    hasMetadata = true;
                }
                zipIn.closeEntry();
            }
            return hasMetadata;
        }
    }

    private boolean validateJsonFile(Path path) throws IOException {
        try {
            objectMapper.readTree(path.toFile());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}