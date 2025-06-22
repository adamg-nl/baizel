package nl.adamg.baizel.internal.common.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.List;

/**
 * Abstraction for filesystem operations to enable testability.
 */
public interface FileSystem {
    boolean exists(Path path);

    List<String> readAllLines(Path path) throws IOException;

    void write(Path path, List<String> lines) throws IOException;

    void createDirectories(Path path) throws IOException;

    void delete(Path path) throws IOException;

    List<Path> findFiles(Path dir, String regexp) throws IOException;

    OutputStream newOutputStream(Path file, OpenOption... options) throws IOException;

    InputStream newInputStream(Path file, OpenOption... options) throws IOException;
}
