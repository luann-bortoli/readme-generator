package io.github.ReadmeGenerator;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.Files.walkFileTree;

@Service
public class ReadmeService {

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
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    fileContents.add(reader(file));
                    return FileVisitResult.CONTINUE;
                }
            });

        } catch (IOException e){

            System.out.println(e.getMessage());
        } finally {

            return fileContents;
        }

    }

}
