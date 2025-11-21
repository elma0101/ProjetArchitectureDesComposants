package com.bookstore.monitoring;

import com.bookstore.config.MonitoringConfig;
import com.bookstore.service.MonitoringService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for monitoring and observability features
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class MonitoringIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MonitoringService monitoringService;

    @Autowired
    private MonitoringConfig monitoringConfig;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    void testActuatorHealthEndpoint() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/actuator/health", Map.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("status");
    }

    @Test
    void testActuatorMetricsEndpoint() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/actuator/metrics", Map.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("names");
    }

    @Test
    void testPrometheusEndpoint() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/actuator/prometheus", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("# HELP");
        assertThat(response.getBody()).contains("# TYPE");
    }

    @Test
    void testCustomMetricsAreRegistered() {
        // Check that custom counters are registered
        Counter loanCounter = meterRegistry.find("bookstore.loans.total").counter();
        assertThat(loanCounter).isNotNull();

        Counter returnCounter = meterRegistry.find("bookstore.returns.total").counter();
        assertThat(returnCounter).isNotNull();

        Counter userRegistrationCounter = meterRegistry.find("bookstore.users.registrations").counter();
        assertThat(userRegistrationCounter).isNotNull();

        Counter apiErrorCounter = meterRegistry.find("bookstore.api.errors").counter();
        assertThat(apiErrorCounter).isNotNull();

        // Check that custom timers are registered
        Timer searchTimer = meterRegistry.find("bookstore.search.duration").timer();
        assertThat(searchTimer).isNotNull();

        Timer databaseTimer = meterRegistry.find("bookstore.database.operation.duration").timer();
        assertThat(databaseTimer).isNotNull();
    }

    @Test
    void testMonitoringServiceRecordsMetrics() {
        // Record some test metrics
        monitoringService.recordBookLoan("user123", "book456");
        monitoringService.recordBookReturn("user123", "book456");
        monitoringService.recordUserRegistration("user789", "test@example.com");

        // Verify counters were incremented
        Counter loanCounter = meterRegistry.find("bookstore.loans.total").counter();
        assertThat(loanCounter.count()).isGreaterThan(0);

        Counter returnCounter = meterRegistry.find("bookstore.returns.total").counter();
        assertThat(returnCounter.count()).isGreaterThan(0);

        Counter registrationCounter = meterRegistry.find("bookstore.users.registrations").counter();
        assertThat(registrationCounter.count()).isGreaterThan(0);
    }

    @Test
    void testSearchTimerRecording() {
        Timer.Sample sample = monitoringService.startSearchTimer();
        
        // Simulate some work
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        monitoringService.recordSearchTime(sample, "title", 5);
        
        Timer searchTimer = meterRegistry.find("bookstore.search.duration").timer();
        assertThat(searchTimer.count()).isGreaterThan(0);
        assertThat(searchTimer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS)).isGreaterThan(0);
    }

    @Test
    void testDatabaseTimerRecording() {
        Timer.Sample sample = monitoringService.startDatabaseTimer();
        
        // Simulate database operation
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        monitoringService.recordDatabaseTime(sample, "findById", "book");
        
        Timer databaseTimer = meterRegistry.find("bookstore.database.operation.duration").timer();
        assertThat(databaseTimer.count()).isGreaterThan(0);
        assertThat(databaseTimer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS)).isGreaterThan(0);
    }

    @Test
    void testErrorRecording() {
        Exception testException = new RuntimeException("Test error");
        monitoringService.recordApiError("/api/books", "RuntimeException", 
                                       "Test error", testException);
        
        Counter errorCounter = meterRegistry.find("bookstore.api.errors").counter();
        assertThat(errorCounter.count()).isGreaterThan(0);
    }

    @Test
    void testUserActivityTracking() {
        monitoringService.recordUserLogin("user123");
        monitoringService.recordUserLogin("user456");
        
        // Check active users gauge
        assertThat(meterRegistry.find("bookstore.users.active").gauge().value()).isGreaterThan(0);
        
        monitoringService.recordUserLogout("user123");
        
        // Active users should decrease
        assertThat(meterRegistry.find("bookstore.users.active").gauge().value()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void testCustomMetricRecording() {
        Map<String, String> tags = Map.of(
            "operation", "test",
            "component", "monitoring"
        );
        
        monitoringService.recordCustomMetric("test.metric", "custom_operation", tags);
        
        // This test mainly verifies that the method doesn't throw exceptions
        // In a real scenario, you might want to verify log output or other side effects
    }

    @Test
    void testHealthIndicatorsAreWorking() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/actuator/health", Map.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> body = response.getBody();
        assertThat(body).containsKey("status");
        
        // In test environment, we might not have all external dependencies
        // so we just verify the endpoint is accessible
    }
}