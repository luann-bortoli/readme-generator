package io.github.ReadmeGenerator;

public class OllamaRequest {
    private String model;
    private String prompt;
    private Boolean stream;

    public OllamaRequest(String model, String prompt, Boolean stream) {
        this.model = model;
        this.prompt = prompt;
        this.stream = stream;
    }
}
