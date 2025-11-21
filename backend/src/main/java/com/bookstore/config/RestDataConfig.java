package com.bookstore.config;

import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import com.bookstore.entity.Loan;
import com.bookstore.entity.Recommendation;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
public class RestDataConfig implements RepositoryRestConfigurer {

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
        // Configure CORS for Spring Data REST
        cors.addMapping("/api/**")
            .allowedOrigins("http://localhost:3000", "http://127.0.0.1:3000")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
        
        // Enable ID exposure for all entities (useful for frontend)
        config.exposeIdsFor(Book.class, Author.class, Loan.class, Recommendation.class);
        
        // Configure pagination settings
        config.setDefaultPageSize(20);
        config.setMaxPageSize(100);
        
        // Return response body for POST and PUT operations
        config.setReturnBodyOnCreate(true);
        config.setReturnBodyOnUpdate(true);
        
        // Configure base path for all REST endpoints
        config.setBasePath("/api");
        
        // Configure entity-specific HTTP method restrictions
        configureEntityExposure(config);
        
        // Configure custom entity lookup
        configureEntityLookup(config);
    }
    
    private void configureEntityExposure(RepositoryRestConfiguration config) {
        // Disable DELETE for books (soft delete should be used instead)
        config.getExposureConfiguration()
              .forDomainType(Book.class)
              .withItemExposure((metdata, httpMethods) -> httpMethods.disable(HttpMethod.DELETE));
        
        // Disable DELETE for recommendations (they should be managed by the recommendation engine)
        config.getExposureConfiguration()
              .forDomainType(Recommendation.class)
              .withItemExposure((metdata, httpMethods) -> httpMethods.disable(HttpMethod.DELETE))
              .withCollectionExposure((metdata, httpMethods) -> httpMethods.disable(HttpMethod.DELETE));
    }
    
    private void configureEntityLookup(RepositoryRestConfiguration config) {
        // Allow books to be looked up by ISBN as well as ID
        config.withEntityLookup()
              .forRepository(com.bookstore.repository.BookRepository.class, Book::getIsbn, 
                           (repository, isbn) -> repository.findByIsbn((String) isbn).orElse(null));
    }
}