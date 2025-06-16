package nl.adamg.baizel.internal.common.io;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Abstraction for filesystem operations to enable testability.
 */
public interface FileSystem {
    boolean fileExists(Path path);

    List<String> readAllLines(Path path) throws IOException;

    void writeLines(Path path, List<String> lines) throws IOException;

    void createDirectories(Path path) throws IOException;

    void delete(Path path) throws IOException;

    List<Path> findFiles(Path outputDir, String regexp) throws IOException;
}
