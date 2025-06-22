package nl.adamg.baizel.internal.common.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.function.BiPredicate;

public class LocalFileSystem implements FileSystem {
    @Override
    public boolean exists(Path path) {
        return Files.exists(path);
    }

    @Override
    public List<String> readAllLines(Path path) throws IOException {
        return Files.readAllLines(path);
    }

    @Override
    public void write(Path path, List<String> lines) throws IOException {
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

    @Override
    public List<Path> findFiles(Path dir, String regexp) throws IOException {
        var matcher = new BiPredicate<Path, BasicFileAttributes>() {
            @Override
            public boolean test(Path path, BasicFileAttributes basicFileAttributes) {
                return Files.isRegularFile(path) && path.toString().matches(regexp);
            }
        };
        try(var stream = Files.find(dir, Integer.MAX_VALUE, matcher)) {
            return stream.toList();
        }
    }

    @Override
    public OutputStream newOutputStream(Path file, OpenOption... options) throws IOException {
        return Files.newOutputStream(file, options);
    }

    @Override
    public InputStream newInputStream(Path file, OpenOption... options) throws IOException {
        return Files.newInputStream(file, options);
    }
}
