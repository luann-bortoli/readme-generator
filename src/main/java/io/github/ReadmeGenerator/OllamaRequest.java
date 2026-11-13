package io.github.ReadmeGenerator;

public record OllamaRequest(String model, String prompt, boolean stream) {
}
