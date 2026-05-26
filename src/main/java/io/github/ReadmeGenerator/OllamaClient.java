package io.github.ReadmeGenerator;

import org.springframework.beans.factory.annotation.Value;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class OllamaClient {

    private final HttpClient client;

    public OllamaClient(HttpClient client) {
        this.client = client;
    }

    @Value("${ollama.url}")
    private String ollamaUrl;

    @Value("${ollama.model}")
    private String ollamaModel;

    private String loadSystemPrompt() {
        try (var is = new org.springframework.core.io.ClassPathResource("SystemPrompt.txt").getInputStream()) {
            return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load system prompt", e);
        }
    }

    public String generate (String userPrompt){

        String prompt = loadSystemPrompt() + userPrompt;

        try {

            OllamaRequest body = new OllamaRequest(ollamaModel, prompt, false);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ollamaUrl))
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println(response.statusCode());
            return response.body();

        } catch (Exception e)
        {
            System.out.println("failed to call api");
            throw new RuntimeException("Failed to call api... \n" + e);
        }
    }

}
