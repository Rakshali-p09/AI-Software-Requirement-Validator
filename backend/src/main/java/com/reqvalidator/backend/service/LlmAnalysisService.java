
package com.reqvalidator.backend.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class LlmAnalysisService {

    private final RestClient restClient;

    @Value("${llm.api.url}")
    private String apiUrl;

    @Value("${llm.api.key}")
    private String apiKey;

    public LlmAnalysisService() {
        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(30000);

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    // Analyze a single requirement
    public String analyzeRequirement(
            String requirement,
            String source,
            String sectionNumber) {

        Map<String, Object> request = new HashMap<>();
        request.put("requirement", requirement);
        request.put("source", source);
        request.put("section_number", sectionNumber);

        try {
            return restClient.post()
                    .uri(apiUrl + "/analyze")
                    .header("x-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(String.class);

        } catch (RestClientException e) {
            throw new IllegalStateException(
                    "AI service is currently unavailable. "
                    + "Please check the AI API server and URL.",
                    e
            );
        }
    }

    // Analyze multiple requirements
    public String analyzeRequirements(
            List<String> requirements) {

        Map<String, Object> request = new HashMap<>();
        request.put("requirements", requirements);

        try {
            return restClient.post()
                    .uri(apiUrl + "/analyze-batch")
                    .header("x-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(String.class);

        } catch (RestClientException e) {
            throw new IllegalStateException(
                    "AI service is currently unavailable. "
                    + "Please check the AI API server and URL.",
                    e
            );
        }
    }

    // Check AI API health
    public String checkHealth() {
        try {
            return restClient.get()
                    .uri(apiUrl + "/health")
                    .retrieve()
                    .body(String.class);

        } catch (RestClientException e) {
            throw new IllegalStateException(
                    "AI service is currently unavailable. "
                    + "Please check the AI API server and URL.",
                    e
            );
        }
    }
}
