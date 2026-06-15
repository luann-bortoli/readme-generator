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

    @Override
    public String toString() {
        return """
        {
          "model": "%s",
          "prompt": "%s",
          "stream": %s
        }
        """.formatted(model, prompt.replace("\"", "\\\""), stream);
    }
}
