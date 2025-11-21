package com.bookstore.controller;

import com.bookstore.dto.*;
import com.bookstore.service.AdvancedBookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Advanced Book Management", description = "Advanced book management operations including bulk operations, availability tracking, statistics, and image management")
public class AdvancedBookController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdvancedBookController.class);
    private static final String UPLOAD_DIR = "uploads/book-images/";
    
    @Autowired
    private AdvancedBookService advancedBookService;
    
    @Operation(summary = "Bulk create books", description = "Create multiple books in a single operation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bulk operation completed",
                    content = @Content(schema = @Schema(implementation = BulkOperationResult.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/bulk")
    public ResponseEntity<BulkOperationResult<com.bookstore.entity.Book>> bulkCreateBooks(
            @Valid @RequestBody BulkBookRequest request) {
        
        logger.info("Received bulk create request for {} books", request.getBooks().size());
        
        BulkOperationResult<com.bookstore.entity.Book> result = advancedBookService.bulkCreateBooks(request);
        
        // Return 207 Multi-Status if there are partial failures, otherwise 200
        HttpStatus status = result.hasErrors() && result.getSuccessCount() > 0 
            ? HttpStatus.MULTI_STATUS 
            : result.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
        
        return ResponseEntity.status(status).body(result);
    }
    
    @Operation(summary = "Bulk update books", description = "Update multiple books in a single operation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bulk operation completed",
                    content = @Content(schema = @Schema(implementation = BulkOperationResult.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/bulk")
    public ResponseEntity<BulkOperationResult<com.bookstore.entity.Book>> bulkUpdateBooks(
            @Valid @RequestBody List<BookUpdateRequest> requests) {
        
        logger.info("Received bulk update request for {} books", requests.size());
        
        BulkOperationResult<com.bookstore.entity.Book> result = advancedBookService.bulkUpdateBooks(requests);
        
        HttpStatus status = result.hasErrors() && result.getSuccessCount() > 0 
            ? HttpStatus.MULTI_STATUS 
            : result.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
        
        return ResponseEntity.status(status).body(result);
    }
    
    @Operation(summary = "Get book availability status", description = "Get detailed availability status and notifications for a specific book")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Book availability status retrieved",
                    content = @Content(schema = @Schema(implementation = BookAvailabilityStatus.class))),
        @ApiResponse(responseCode = "404", description = "Book not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}/availability")
    public ResponseEntity<BookAvailabilityStatus> getBookAvailabilityStatus(
            @Parameter(description = "Book ID", required = true) @PathVariable Long id) {
        
        BookAvailabilityStatus status = advancedBookService.getBookAvailabilityStatus(id);
        return ResponseEntity.ok(status);
    }
    
    @Operation(summary = "Get books with low stock", description = "Get all books with stock below the specified threshold")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Low stock books retrieved",
                    content = @Content(schema = @Schema(implementation = BookAvailabilityStatus.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/low-stock")
    public ResponseEntity<List<BookAvailabilityStatus>> getBooksWithLowStock(
            @Parameter(description = "Stock threshold", example = "2") 
            @RequestParam(defaultValue = "2") int threshold) {
        
        List<BookAvailabilityStatus> lowStockBooks = advancedBookService.getBooksWithLowStock(threshold);
        return ResponseEntity.ok(lowStockBooks);
    }
    
    @Operation(summary = "Get book statistics", description = "Get comprehensive statistics about books in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Book statistics retrieved",
                    content = @Content(schema = @Schema(implementation = BookStatistics.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/statistics")
    public ResponseEntity<BookStatistics> getBookStatistics() {
        BookStatistics statistics = advancedBookService.getBookStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    @Operation(summary = "Upload book image", description = "Upload a cover image for a specific book")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Image uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file or request"),
        @ApiResponse(responseCode = "404", description = "Book not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/{id}/image")
    public ResponseEntity<String> uploadBookImage(
            @Parameter(description = "Book ID", required = true) @PathVariable Long id,
            @Parameter(description = "Image file", required = true) @RequestParam("file") MultipartFile file) {
        
        try {
            String imageUrl = advancedBookService.uploadBookImage(id, file);
            return ResponseEntity.ok(imageUrl);
        } catch (IOException e) {
            logger.error("Failed to upload image for book {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to upload image: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @Operation(summary = "Get book image", description = "Retrieve the cover image for a specific book")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Image retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Image not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}/image/{filename}")
    public ResponseEntity<Resource> getBookImage(
            @Parameter(description = "Book ID", required = true) @PathVariable Long id,
            @Parameter(description = "Image filename", required = true) @PathVariable String filename) {
        
        try {
            Path filePath = Paths.get(UPLOAD_DIR).resolve(filename);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                // Determine content type
                String contentType = "image/jpeg"; // Default
                if (filename.toLowerCase().endsWith(".png")) {
                    contentType = "image/png";
                } else if (filename.toLowerCase().endsWith(".gif")) {
                    contentType = "image/gif";
                }
                
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve image {} for book {}: {}", filename, id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Operation(summary = "Delete book image", description = "Delete the cover image for a specific book")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Image deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Book not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}/image")
    public ResponseEntity<Void> deleteBookImage(
            @Parameter(description = "Book ID", required = true) @PathVariable Long id) {
        
        try {
            advancedBookService.deleteBookImage(id);
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            logger.error("Failed to delete image for book {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}