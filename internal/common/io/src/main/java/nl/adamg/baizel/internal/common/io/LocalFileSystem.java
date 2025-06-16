package nl.adamg.baizel.internal.common.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class LocalFileSystem implements FileSystem {
    @Override
    public boolean fileExists(Path path) {
        return Files.exists(path);
    }

    @Override
    public List<String> readAllLines(Path path) throws IOException {
        return Files.readAllLines(path);
    }

    @Override
    public void writeLines(Path path, List<String> lines) throws IOException {
        Files.write(path, lines);
    }

    @Override
    public void createDirectories(Path path) throws IOException {
        Files.createDirectories(path);
    }

    @Override
    public void delete(Path path) throws IOException {
        FileSystemUtil.delete(path);
    }
}
