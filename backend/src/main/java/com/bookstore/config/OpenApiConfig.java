package com.bookstore.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3.0 configuration for the Bookstore Management API.
 * Configures Swagger UI, API documentation, security schemes, and server information.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bookstoreOpenAPI() {
        // Development server configuration
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8080");
        devServer.setDescription("Development environment");

        // Production server configuration (example)
        Server prodServer = new Server();
        prodServer.setUrl("https://api.bookstore.com");
        prodServer.setDescription("Production environment");

        // Contact information
        Contact contact = new Contact();
        contact.setEmail("admin@bookstore.com");
        contact.setName("Bookstore API Team");
        contact.setUrl("https://bookstore.com/support");

        // License information
        License mitLicense = new License()
                .name("MIT License")
                .url("https://choosealicense.com/licenses/mit/");

        // API information
        Info info = new Info()
                .title("Bookstore Management API")
                .version("1.0.0")
                .contact(contact)
                .description("""
                    # Bookstore Management API
                    
                    This comprehensive REST API provides endpoints for managing books, authors, and loans in a library system.
                    
                    ## Features
                    - **Book Management**: Complete CRUD operations for books with advanced search capabilities
                    - **Author Management**: Manage authors and their relationships with books
                    - **Loan Management**: Track book borrowing, returns, and overdue items
                    - **Recommendation Engine**: AI-powered book recommendations based on user behavior
                    - **External Integration**: FeignClient and WebClient support for external services
                    - **Spring Data REST**: Auto-generated REST endpoints with HATEOAS support
                    
                    ## Technology Stack
                    - Spring Boot 3.x with Spring Data REST
                    - PostgreSQL database with JPA/Hibernate
                    - Spring Security for authentication
                    - OpenFeign and WebClient for external service integration
                    - Reactive programming support with WebFlux
                    
                    ## Getting Started
                    1. All endpoints support pagination using `page` and `size` parameters
                    2. Use the `/api` prefix for all custom endpoints
                    3. Spring Data REST endpoints are available at `/api/books`, `/api/authors`, `/api/loans`
                    4. Interactive API testing is available through this Swagger UI interface
                    """)
                .license(mitLicense);

        // Security scheme for JWT authentication
        SecurityScheme jwtSecurityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT token for API authentication");

        // API tags for better organization
        Tag booksTag = new Tag()
                .name("Books")
                .description("Book management operations including CRUD, search, and filtering");

        Tag authorsTag = new Tag()
                .name("Authors")
                .description("Author management operations and book relationships");

        Tag loansTag = new Tag()
                .name("Loans")
                .description("Loan management for book borrowing and returns");

        Tag recommendationsTag = new Tag()
                .name("Recommendations")
                .description("AI-powered book recommendation engine");

        Tag searchTag = new Tag()
                .name("Search")
                .description("Advanced search and filtering capabilities");

        Tag externalTag = new Tag()
                .name("External Services")
                .description("Integration with external book and author services");

        Tag reactiveTag = new Tag()
                .name("Reactive External Services")
                .description("Reactive operations for external service integration");

        Tag healthTag = new Tag()
                .name("Health")
                .description("Application health and monitoring endpoints");

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", jwtSecurityScheme))
                .tags(List.of(
                        booksTag,
                        authorsTag,
                        loansTag,
                        recommendationsTag,
                        searchTag,
                        externalTag,
                        reactiveTag,
                        healthTag
                ));
    }
}