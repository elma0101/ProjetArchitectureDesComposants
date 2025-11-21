package com.bookstore.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "bookstore")
public class ExternalConfigurationProperties {

    private Cors cors = new Cors();
    private RateLimiting rateLimiting = new RateLimiting();
    private ExternalServices externalServices = new ExternalServices();
    private Monitoring monitoring = new Monitoring();
    private FileUpload fileUpload = new FileUpload();
    private Map<String, Boolean> featureToggles;

    // Getters and setters
    public Cors getCors() {
        return cors;
    }

    public void setCors(Cors cors) {
        this.cors = cors;
    }

    public RateLimiting getRateLimiting() {
        return rateLimiting;
    }

    public void setRateLimiting(RateLimiting rateLimiting) {
        this.rateLimiting = rateLimiting;
    }

    public ExternalServices getExternalServices() {
        return externalServices;
    }

    public void setExternalServices(ExternalServices externalServices) {
        this.externalServices = externalServices;
    }

    public Monitoring getMonitoring() {
        return monitoring;
    }

    public void setMonitoring(Monitoring monitoring) {
        this.monitoring = monitoring;
    }

    public FileUpload getFileUpload() {
        return fileUpload;
    }

    public void setFileUpload(FileUpload fileUpload) {
        this.fileUpload = fileUpload;
    }

    public Map<String, Boolean> getFeatureToggles() {
        return featureToggles;
    }

    public void setFeatureToggles(Map<String, Boolean> featureToggles) {
        this.featureToggles = featureToggles;
    }

    public static class Cors {
        private String allowedOrigins;
        private String allowedMethods = "GET,POST,PUT,DELETE,OPTIONS";
        private String allowedHeaders = "*";
        private boolean allowCredentials = true;
        private long maxAge = 3600;

        // Getters and setters
        public String getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(String allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }

        public String getAllowedMethods() {
            return allowedMethods;
        }

        public void setAllowedMethods(String allowedMethods) {
            this.allowedMethods = allowedMethods;
        }

        public String getAllowedHeaders() {
            return allowedHeaders;
        }

        public void setAllowedHeaders(String allowedHeaders) {
            this.allowedHeaders = allowedHeaders;
        }

        public boolean isAllowCredentials() {
            return allowCredentials;
        }

        public void setAllowCredentials(boolean allowCredentials) {
            this.allowCredentials = allowCredentials;
        }

        public long getMaxAge() {
            return maxAge;
        }

        public void setMaxAge(long maxAge) {
            this.maxAge = maxAge;
        }
    }

    public static class RateLimiting {
        private boolean enabled = true;
        private int requestsPerMinute = 100;
        private int burstCapacity = 200;

        // Getters and setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getRequestsPerMinute() {
            return requestsPerMinute;
        }

        public void setRequestsPerMinute(int requestsPerMinute) {
            this.requestsPerMinute = requestsPerMinute;
        }

        public int getBurstCapacity() {
            return burstCapacity;
        }

        public void setBurstCapacity(int burstCapacity) {
            this.burstCapacity = burstCapacity;
        }
    }

    public static class ExternalServices {
        private ServiceConfig bookService = new ServiceConfig();
        private ServiceConfig authorService = new ServiceConfig();

        public ServiceConfig getBookService() {
            return bookService;
        }

        public void setBookService(ServiceConfig bookService) {
            this.bookService = bookService;
        }

        public ServiceConfig getAuthorService() {
            return authorService;
        }

        public void setAuthorService(ServiceConfig authorService) {
            this.authorService = authorService;
        }

        public static class ServiceConfig {
            private String url;
            private int timeout = 5000;
            private int retryAttempts = 3;

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public int getTimeout() {
                return timeout;
            }

            public void setTimeout(int timeout) {
                this.timeout = timeout;
            }

            public int getRetryAttempts() {
                return retryAttempts;
            }

            public void setRetryAttempts(int retryAttempts) {
                this.retryAttempts = retryAttempts;
            }
        }
    }

    public static class Monitoring {
        private boolean enabled = true;
        private long metricsCollectionInterval = 60000;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getMetricsCollectionInterval() {
            return metricsCollectionInterval;
        }

        public void setMetricsCollectionInterval(long metricsCollectionInterval) {
            this.metricsCollectionInterval = metricsCollectionInterval;
        }
    }

    public static class FileUpload {
        private String maxFileSize = "10MB";
        private String maxRequestSize = "50MB";
        private String uploadDir = "/var/bookstore/uploads";

        public String getMaxFileSize() {
            return maxFileSize;
        }

        public void setMaxFileSize(String maxFileSize) {
            this.maxFileSize = maxFileSize;
        }

        public String getMaxRequestSize() {
            return maxRequestSize;
        }

        public void setMaxRequestSize(String maxRequestSize) {
            this.maxRequestSize = maxRequestSize;
        }

        public String getUploadDir() {
            return uploadDir;
        }

        public void setUploadDir(String uploadDir) {
            this.uploadDir = uploadDir;
        }
    }
}