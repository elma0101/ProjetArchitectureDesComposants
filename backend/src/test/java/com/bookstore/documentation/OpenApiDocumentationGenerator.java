package com.bookstore.documentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class that generates and exports the OpenAPI specification to files.
 * This can be used for documentation generation and API contract validation.
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class OpenApiDocumentationGenerator {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void generateOpenApiSpecification() throws Exception {
        // Get the OpenAPI specification
        MvcResult result = mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andReturn();

        String openApiJson = result.getResponse().getContentAsString();
        
        // Parse and pretty-print the JSON
        JsonNode openApiDoc = objectMapper.readTree(openApiJson);
        ObjectMapper prettyMapper = new ObjectMapper();
        prettyMapper.enable(SerializationFeature.INDENT_OUTPUT);
        String prettyJson = prettyMapper.writeValueAsString(openApiDoc);

        // Create target directory if it doesn't exist
        File targetDir = new File("target/generated-docs");
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        // Write JSON specification
        writeToFile("target/generated-docs/openapi.json", prettyJson);
        
        // Generate a simple markdown documentation
        String markdownDoc = generateMarkdownDocumentation(openApiDoc);
        writeToFile("target/generated-docs/api-documentation.md", markdownDoc);

        System.out.println("OpenAPI specification generated:");
        System.out.println("- JSON: target/generated-docs/openapi.json");
        System.out.println("- Markdown: target/generated-docs/api-documentation.md");
    }

    private void writeToFile(String filename, String content) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(content);
        }
    }

    private String generateMarkdownDocumentation(JsonNode openApiDoc) {
        StringBuilder md = new StringBuilder();
        
        // Title and description
        JsonNode info = openApiDoc.get("info");
        md.append("# ").append(info.get("title").asText()).append("\n\n");
        md.append("**Version:** ").append(info.get("version").asText()).append("\n\n");
        
        if (info.has("description")) {
            md.append(info.get("description").asText()).append("\n\n");
        }

        // Servers
        JsonNode servers = openApiDoc.get("servers");
        if (servers != null && servers.isArray()) {
            md.append("## Servers\n\n");
            for (JsonNode server : servers) {
                md.append("- **").append(server.get("description").asText()).append("**: ");
                md.append(server.get("url").asText()).append("\n");
            }
            md.append("\n");
        }

        // Tags
        JsonNode tags = openApiDoc.get("tags");
        if (tags != null && tags.isArray()) {
            md.append("## API Categories\n\n");
            for (JsonNode tag : tags) {
                md.append("### ").append(tag.get("name").asText()).append("\n");
                if (tag.has("description")) {
                    md.append(tag.get("description").asText()).append("\n");
                }
                md.append("\n");
            }
        }

        // Paths summary
        JsonNode paths = openApiDoc.get("paths");
        if (paths != null) {
            md.append("## Endpoints Summary\n\n");
            md.append("| Method | Path | Summary |\n");
            md.append("|--------|------|----------|\n");
            
            paths.fieldNames().forEachRemaining(path -> {
                JsonNode pathItem = paths.get(path);
                pathItem.fieldNames().forEachRemaining(method -> {
                    JsonNode operation = pathItem.get(method);
                    if (operation.has("summary")) {
                        md.append("| ").append(method.toUpperCase()).append(" | ");
                        md.append(path).append(" | ");
                        md.append(operation.get("summary").asText()).append(" |\n");
                    }
                });
            });
            md.append("\n");
        }

        // Authentication
        JsonNode components = openApiDoc.get("components");
        if (components != null && components.has("securitySchemes")) {
            md.append("## Authentication\n\n");
            md.append("This API uses Bearer Token authentication (JWT).\n\n");
            md.append("Include the token in the Authorization header:\n");
            md.append("```\nAuthorization: Bearer <your-jwt-token>\n```\n\n");
        }

        // Common response codes
        md.append("## Common Response Codes\n\n");
        md.append("| Code | Description |\n");
        md.append("|------|-------------|\n");
        md.append("| 200 | Success |\n");
        md.append("| 201 | Created |\n");
        md.append("| 400 | Bad Request |\n");
        md.append("| 401 | Unauthorized |\n");
        md.append("| 404 | Not Found |\n");
        md.append("| 500 | Internal Server Error |\n\n");

        // Footer
        md.append("---\n\n");
        md.append("*Generated from OpenAPI specification*\n");

        return md.toString();
    }
}