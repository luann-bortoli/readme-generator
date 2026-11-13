package io.github.ReadmeGenerator;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;
import java.util.Set;

import static java.nio.file.Files.walkFileTree;

@Service
public class ReadmeService {

    private static final int MAX_PROJECT_CONTEXT_CHARS = 60_000;
    private static final int MAX_FILE_CONTENT_CHARS = 12_000;

    private static final Set<String> IGNORED_DIRECTORIES = Set.of(
            ".git",
            ".idea",
            ".vscode",
            ".gradle",
            ".mvn",
            "target",
            "build",
            "out",
            "dist",
            "node_modules",
            "coverage",
            "__pycache__"
    );

    private static final Set<String> INCLUDED_EXTENSIONS = Set.of(
            ".java",
            ".py",
            ".js",
            ".ts",
            ".jsx",
            ".tsx",
            ".html",
            ".css",
            ".scss",
            ".json",
            ".xml",
            ".yml",
            ".yaml",
            ".properties",
            ".md",
            ".txt",
            ".gradle",
            ".kt",
            ".kts",
            ".go",
            ".rs",
            ".php",
            ".rb",
            ".cs",
            ".c",
            ".cpp",
            ".h",
            ".hpp",
            ".sql"
    );

    private static final Set<String> INCLUDED_FILENAMES = Set.of(
            "dockerfile",
            "makefile",
            "pom.xml",
            "build.gradle",
            "settings.gradle",
            "gradlew",
            "mvnw"
    );

    public static String reader(Path path) throws IOException {

        StringBuilder lines = new StringBuilder();
        String line;

        try (BufferedReader br = new BufferedReader(new FileReader(path.toString()))) {
            while((line = br.readLine()) != null){
                lines.append(line).append("\n");
            }

        } catch(FileNotFoundException e) {

            System.out.println(e.getMessage());
        }

        return lines.toString();
    }

    public List<String> walkPath(String targetPath) {

        Path path = Paths.get(targetPath).toAbsolutePath();
        List<String> fileContents = new ArrayList<>();

        for (ProjectFile projectFile : collectProjectFiles(path)) {
            fileContents.add(projectFile.content());
        }

        return fileContents;

    }

    public String buildProjectContext(String targetPath) {

        Path path = Paths.get(targetPath).toAbsolutePath();
        List<ProjectFile> projectFiles = collectProjectFiles(path);

        StringBuilder context = new StringBuilder();
        context.append("Project path: ").append(path).append("\n\n");
        context.append("Files included:\n");

        for (ProjectFile projectFile : projectFiles) {
            context.append("- ").append(projectFile.relativePath()).append("\n");
        }

        context.append("\nProject files:\n\n");

        for (ProjectFile projectFile : projectFiles) {
            String content = truncateFileContent(projectFile.content());
            String section = """
                    --- FILE: %s ---
                    %s
                    --- END FILE ---

                    """.formatted(projectFile.relativePath(), content);

            if (context.length() + section.length() > MAX_PROJECT_CONTEXT_CHARS) {
                context.append("[Project context truncated because it reached the maximum prompt size.]\n");
                break;
            }

            context.append(section);
        }

        return context.toString();

    }

    public Path writeReadme(String targetPath, String markdown) throws IOException {

        Path projectPath = Paths.get(targetPath).toAbsolutePath();
        Path readmePath = projectPath.resolve("README.md");

        Files.writeString(
                readmePath,
                markdown,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        );

        return readmePath;

    }

    private List<ProjectFile> collectProjectFiles(Path path) {

        List<ProjectFile> projectFiles = new ArrayList<>();

        try{

            walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (shouldIgnoreDirectory(dir)) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (attrs.isRegularFile() && shouldReadFile(file)) {
                        String relativePath = path.relativize(file)
                                .toString()
                                .replace("\\", "/");

                        projectFiles.add(new ProjectFile(relativePath, reader(file)));
                    }

                    return FileVisitResult.CONTINUE;
                }
            });

        } catch (IOException e){

            System.out.println(e.getMessage());
        }

        return projectFiles;

    }

    private String truncateFileContent(String content) {
        if (content.length() <= MAX_FILE_CONTENT_CHARS) {
            return content;
        }

        return content.substring(0, MAX_FILE_CONTENT_CHARS)
                + "\n[File truncated because it reached the maximum per-file prompt size.]\n";
    }

    private boolean shouldIgnoreDirectory(Path directory) {
        Path name = directory.getFileName();

        return name != null && IGNORED_DIRECTORIES.contains(name.toString().toLowerCase(Locale.ROOT));
    }

    private boolean shouldReadFile(Path file) {
        String fileName = file.getFileName().toString().toLowerCase(Locale.ROOT);

        if (INCLUDED_FILENAMES.contains(fileName)) {
            return true;
        }

        return INCLUDED_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }

    private record ProjectFile(String relativePath, String content) {
    }

}
