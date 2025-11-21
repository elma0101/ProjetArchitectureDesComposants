package com.bookstore.client;

import com.bookstore.exception.BookNotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

/**
 * Custom error decoder for Feign clients to handle HTTP errors gracefully
 */
public class FeignErrorDecoder implements ErrorDecoder {
    
    private static final Logger logger = LoggerFactory.getLogger(FeignErrorDecoder.class);
    private final ErrorDecoder defaultErrorDecoder = new Default();
    
    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus status = HttpStatus.valueOf(response.status());
        
        logger.warn("Feign client error - Method: {}, Status: {}, Reason: {}", 
                   methodKey, response.status(), response.reason());
        
        switch (status) {
            case NOT_FOUND:
                if (methodKey.contains("Book")) {
                    return new BookNotFoundException("Book not found in external service");
                }
                return new RuntimeException("Resource not found in external service");
                
            case BAD_REQUEST:
                return new IllegalArgumentException("Invalid request to external service: " + response.reason());
                
            case UNAUTHORIZED:
                return new RuntimeException("Unauthorized access to external service");
                
            case FORBIDDEN:
                return new RuntimeException("Forbidden access to external service");
                
            case INTERNAL_SERVER_ERROR:
                return new RuntimeException("External service internal error");
                
            case SERVICE_UNAVAILABLE:
                return new RuntimeException("External service unavailable");
                
            case REQUEST_TIMEOUT:
            case GATEWAY_TIMEOUT:
                return new RuntimeException("External service timeout");
                
            default:
                return defaultErrorDecoder.decode(methodKey, response);
        }
    }
}