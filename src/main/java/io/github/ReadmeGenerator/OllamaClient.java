package io.github.ReadmeGenerator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class OllamaClient {

    private final HttpClient client;
    private final ObjectMapper objectMapper;

    public OllamaClient(HttpClient client, ObjectMapper objectMapper) {
        this.client = client;
        this.objectMapper = objectMapper;
    }

    @Value("${ollama.url}")
    private String ollamaUrl;

    @Value("${ollama.model}")
    private String ollamaModel;

    private String loadSystemPrompt() {
        try (var is = new org.springframework.core.io.ClassPathResource("SystemPrompt").getInputStream()) {
            return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load system prompt", e);
        }
    }

    public String generate (String userPrompt){

        String prompt = loadSystemPrompt() + userPrompt;

        try {

            OllamaRequest body = new OllamaRequest(ollamaModel, prompt, false);
            String jsonBody = objectMapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ollamaUrl))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("Ollama returned status " + response.statusCode() + ": " + response.body());
            }

            OllamaResponse ollamaResponse = objectMapper.readValue(response.body(), OllamaResponse.class);

            System.out.println(ollamaResponse.response());
            return ollamaResponse.response();

        } catch (Exception e)
        {
            System.out.println("failed to call api");
            throw new RuntimeException("Failed to call api... \n" + e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OllamaResponse(String response) {
    }

}
