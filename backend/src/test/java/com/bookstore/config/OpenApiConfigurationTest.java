package com.bookstore.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for OpenAPI configuration and documentation generation.
 * Validates that the OpenAPI specification is properly generated and accessible.
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class OpenApiConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldGenerateOpenApiDocumentation() throws Exception {
        MvcResult result = mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        JsonNode openApiDoc = objectMapper.readTree(content);

        // Validate OpenAPI version
        assertThat(openApiDoc.get("openapi").asText()).startsWith("3.0");

        // Validate API info
        JsonNode info = openApiDoc.get("info");
        assertThat(info.get("title").asText()).isEqualTo("Bookstore Management API");
        assertThat(info.get("version").asText()).isEqualTo("1.0.0");
        assertThat(info.get("description").asText()).contains("Bookstore Management API");

        // Validate servers
        JsonNode servers = openApiDoc.get("servers");
        assertThat(servers).isNotNull();
        assertThat(servers.isArray()).isTrue();
        assertThat(servers.size()).isGreaterThan(0);

        // Validate tags
        JsonNode tags = openApiDoc.get("tags");
        assertThat(tags).isNotNull();
        assertThat(tags.isArray()).isTrue();
        
        // Check for expected tags
        boolean hasBookTag = false;
        boolean hasLoanTag = false;
        boolean hasRecommendationTag = false;
        
        for (JsonNode tag : tags) {
            String tagName = tag.get("name").asText();
            if ("Books".equals(tagName)) hasBookTag = true;
            if ("Loans".equals(tagName)) hasLoanTag = true;
            if ("Recommendations".equals(tagName)) hasRecommendationTag = true;
        }
        
        assertThat(hasBookTag).isTrue();
        assertThat(hasLoanTag).isTrue();
        assertThat(hasRecommendationTag).isTrue();

        // Validate paths
        JsonNode paths = openApiDoc.get("paths");
        assertThat(paths).isNotNull();
        
        // Check for key endpoints
        assertThat(paths.has("/api/books/search/findByTitle")).isTrue();
        assertThat(paths.has("/api/loan-management/borrow")).isTrue();
        assertThat(paths.has("/api/recommendations/{userId}")).isTrue();
        assertThat(paths.has("/health")).isTrue();

        // Validate components and schemas
        JsonNode components = openApiDoc.get("components");
        assertThat(components).isNotNull();
        
        JsonNode schemas = components.get("schemas");
        assertThat(schemas).isNotNull();
        
        // Check for entity schemas
        assertThat(schemas.has("Book")).isTrue();
        assertThat(schemas.has("Loan")).isTrue();
        assertThat(schemas.has("Recommendation")).isTrue();

        // Validate security schemes
        JsonNode securitySchemes = components.get("securitySchemes");
        assertThat(securitySchemes).isNotNull();
        assertThat(securitySchemes.has("bearerAuth")).isTrue();
    }

    @Test
    void shouldAccessSwaggerUI() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());
                
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html"));
    }

    @Test
    void shouldValidateBookSchemaInOpenApiDoc() throws Exception {
        MvcResult result = mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        JsonNode openApiDoc = objectMapper.readTree(content);

        JsonNode bookSchema = openApiDoc.get("components").get("schemas").get("Book");
        assertThat(bookSchema).isNotNull();

        JsonNode properties = bookSchema.get("properties");
        assertThat(properties).isNotNull();

        // Validate key properties exist
        assertThat(properties.has("id")).isTrue();
        assertThat(properties.has("title")).isTrue();
        assertThat(properties.has("isbn")).isTrue();
        assertThat(properties.has("description")).isTrue();
        assertThat(properties.has("publicationYear")).isTrue();
        assertThat(properties.has("genre")).isTrue();
        assertThat(properties.has("availableCopies")).isTrue();
        assertThat(properties.has("totalCopies")).isTrue();

        // Validate property descriptions and examples
        JsonNode titleProperty = properties.get("title");
        assertThat(titleProperty.get("description").asText()).contains("Title of the book");
        assertThat(titleProperty.get("example").asText()).isNotEmpty();

        JsonNode isbnProperty = properties.get("isbn");
        assertThat(isbnProperty.get("description").asText()).contains("International Standard Book Number");
        assertThat(isbnProperty.get("example").asText()).matches("\\d{3}-\\d{10}");
    }

    @Test
    void shouldValidateEndpointDocumentation() throws Exception {
        MvcResult result = mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        JsonNode openApiDoc = objectMapper.readTree(content);

        // Validate book search endpoint
        JsonNode bookSearchPath = openApiDoc.get("paths").get("/api/books/search/findByTitle");
        assertThat(bookSearchPath).isNotNull();

        JsonNode getOperation = bookSearchPath.get("get");
        assertThat(getOperation).isNotNull();
        assertThat(getOperation.get("summary").asText()).isEqualTo("Search books by title");
        assertThat(getOperation.get("description").asText()).contains("Search for books by title");

        // Validate parameters
        JsonNode parameters = getOperation.get("parameters");
        assertThat(parameters).isNotNull();
        assertThat(parameters.isArray()).isTrue();

        // Check for title parameter
        boolean hasTitleParam = false;
        for (JsonNode param : parameters) {
            if ("title".equals(param.get("name").asText())) {
                hasTitleParam = true;
                assertThat(param.get("required").asBoolean()).isTrue();
                assertThat(param.get("description").asText()).contains("Title to search for");
                break;
            }
        }
        assertThat(hasTitleParam).isTrue();

        // Validate responses
        JsonNode responses = getOperation.get("responses");
        assertThat(responses).isNotNull();
        assertThat(responses.has("200")).isTrue();
        assertThat(responses.has("400")).isTrue();

        JsonNode successResponse = responses.get("200");
        assertThat(successResponse.get("description").asText()).isEqualTo("Books found successfully");
    }

    @Test
    void shouldValidateLoanEndpointDocumentation() throws Exception {
        MvcResult result = mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        JsonNode openApiDoc = objectMapper.readTree(content);

        // Validate loan borrow endpoint
        JsonNode loanBorrowPath = openApiDoc.get("paths").get("/api/loan-management/borrow");
        assertThat(loanBorrowPath).isNotNull();

        JsonNode postOperation = loanBorrowPath.get("post");
        assertThat(postOperation).isNotNull();
        assertThat(postOperation.get("summary").asText()).isEqualTo("Borrow a book");
        assertThat(postOperation.get("description").asText()).contains("Create a new loan record");

        // Validate request body
        JsonNode requestBody = postOperation.get("requestBody");
        assertThat(requestBody).isNotNull();
        assertThat(requestBody.get("required").asBoolean()).isTrue();

        // Validate responses
        JsonNode responses = postOperation.get("responses");
        assertThat(responses).isNotNull();
        assertThat(responses.has("201")).isTrue();
        assertThat(responses.has("400")).isTrue();
        assertThat(responses.has("404")).isTrue();
    }

    @Test
    void shouldValidateHealthEndpointDocumentation() throws Exception {
        MvcResult result = mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        JsonNode openApiDoc = objectMapper.readTree(content);

        // Validate health endpoint
        JsonNode healthPath = openApiDoc.get("paths").get("/health");
        assertThat(healthPath).isNotNull();

        JsonNode getOperation = healthPath.get("get");
        assertThat(getOperation).isNotNull();
        assertThat(getOperation.get("summary").asText()).isEqualTo("Health check endpoint");
        assertThat(getOperation.get("description").asText()).contains("Returns the current health status");

        // Validate response with example
        JsonNode responses = getOperation.get("responses");
        assertThat(responses).isNotNull();
        assertThat(responses.has("200")).isTrue();

        JsonNode successResponse = responses.get("200");
        JsonNode content200 = successResponse.get("content");
        assertThat(content200).isNotNull();
        
        JsonNode jsonContent = content200.get("application/json");
        assertThat(jsonContent).isNotNull();
        
        JsonNode examples = jsonContent.get("examples");
        if (examples != null) {
            assertThat(examples.has("Healthy response")).isTrue();
        }
    }
}