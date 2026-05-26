package io.github.ReadmeGenerator;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
public class CliRunner implements CommandLineRunner {

    ReadmeService readmeService;
    OllamaClient ollamaClient;

    public CliRunner(ReadmeService readmeService, OllamaClient ollamaClient) {
        this.readmeService = readmeService;
        this.ollamaClient = ollamaClient;
    }

    public CliRunner() {}

    @Override
    public void run(String... args) throws Exception {

        Scanner scanner = new Scanner(System.in);

        System.out.println("Sistema iniciado");

        while (true) {

            System.out.println("write the path");
            String path = scanner.nextLine();

            ollamaClient.generate(path);

        }

    }

}
