package io.github.ReadmeGenerator;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

@Component
public class CliRunner implements CommandLineRunner {

    ReadmeService readmeService;
    OllamaClient ollamaClient;

    public CliRunner(ReadmeService readmeService, OllamaClient ollamaClient) {
        this.readmeService = readmeService;
        this.ollamaClient = ollamaClient;
    }

    @Override
    public void run(String... args) throws Exception {

        Scanner scanner = new Scanner(System.in);

        System.out.println("Sistema iniciado");
        System.out.println("Type a project path to generate README.md, or type exit to stop.");

        while (true) {

            System.out.println("write the path");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Stopping application.");
                break;
            }

            if (input.isBlank()) {
                System.out.println("Path cannot be empty.");
                continue;
            }

            Path projectPath = Paths.get(input).toAbsolutePath();

            if (!Files.exists(projectPath)) {
                System.out.println("Path does not exist: " + projectPath);
                continue;
            }

            if (!Files.isDirectory(projectPath)) {
                System.out.println("Path is not a directory: " + projectPath);
                continue;
            }

            try {
                System.out.println("Reading project files...");
                String projectContext = readmeService.buildProjectContext(projectPath.toString());

                System.out.println("Generating README with Ollama...");
                String readme = ollamaClient.generate(projectContext);

                Path readmePath = readmeService.writeReadme(projectPath.toString(), readme);
                System.out.println("README generated at: " + readmePath);
            } catch (Exception e) {
                System.out.println("Failed to generate README: " + e.getMessage());
            }

        }

    }

}
