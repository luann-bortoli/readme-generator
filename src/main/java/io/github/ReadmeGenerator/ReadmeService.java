package io.github.ReadmeGenerator;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;
import java.util.Set;

import static java.nio.file.Files.walkFileTree;

@Service
public class ReadmeService {

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
                        fileContents.add(reader(file));
                    }

                    return FileVisitResult.CONTINUE;
                }
            });

        } catch (IOException e){

            System.out.println(e.getMessage());
        } finally {

            return fileContents;
        }

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

}
