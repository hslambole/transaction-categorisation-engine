package com.assessment.transaction_categorisation_engine.client;

import com.assessment.transaction_categorisation_engine.config.LlmProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class HttpLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(HttpLlmClient.class);

    private static final String PROMPT_TEMPLATE = """
            Categorise the merchant name into a spend category.
            Return ONLY valid JSON with keys: category, sub_category, confidence.
            confidence must be a number between 0.0 and 1.0.
            Merchant: %s
            """;

    private final LlmProperties properties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public HttpLlmClient(LlmProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.create();
    }

    @Override
    public LlmCategoryResponse classify(String merchantName) {
        if (properties.getApiUrl() == null || properties.getApiUrl().isBlank()) {
            throw new IllegalStateException("llm.api-url is not configured");
        }

        String prompt = PROMPT_TEMPLATE.formatted(merchantName);
        RestClient.RequestBodySpec request = restClient.post()
                .uri(properties.getApiUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("merchantName", merchantName, "prompt", prompt));

        if (properties.getApiKey() != null && !properties.getApiKey().isBlank()) {
            request = request.header("Authorization", "Bearer " + properties.getApiKey());
        }

        String raw = request.retrieve().body(String.class);
        log.debug("LLM raw response for merchant '{}': {}", merchantName, raw);
        return parse(raw);
    }

    LlmCategoryResponse parse(String raw) {
        try {
            JsonNode root = objectMapper.readTree(raw);
            if (root.has("category")) {
                return objectMapper.treeToValue(root, LlmCategoryResponse.class);
            }
            if (root.has("content")) {
                return objectMapper.readValue(root.get("content").asText(), LlmCategoryResponse.class);
            }
            throw new IllegalArgumentException("LLM response missing category");
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to parse LLM response", ex);
        }
    }
}
