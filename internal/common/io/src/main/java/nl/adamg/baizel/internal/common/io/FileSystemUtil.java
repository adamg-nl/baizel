package nl.adamg.baizel.internal.common.io;

 import nl.adamg.baizel.internal.common.util.Exceptions;
import nl.adamg.baizel.internal.common.util.functions.Function;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public final class FileSystemUtil {
    public static List<Path> list(Path parentDir) throws IOException {
        try (var stream = Files.list(parentDir)) {
            return stream.collect(Collectors.toList());
        }
    }

    public static List<Path> listFiles(Path parentDir) throws IOException {
        try (var stream = Files.list(parentDir)) {
            return stream.filter(Files::isRegularFile).collect(Collectors.toList());
        }
    }

    public static List<Path> listDirs(Path parentDir) throws IOException {
        try (var stream = Files.list(parentDir)) {
            return stream.filter(Files::isDirectory).collect(Collectors.toList());
        }
    }

    /** wrapper for {@link Files#walkFileTree} that supports checked exceptions */
    public static <T extends Exception> void visitDirsRecursively(
            Path root, Class<T> exceptionType, Function<Path, FileVisitResult, T> visitor)
            throws IOException, T {
        var exception = new AtomicReference<Exception>();
        Files.walkFileTree(
                root,
                new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                        try {
                            return visitor.apply(dir);
                        } catch (Exception e) {
                            exception.set(e);
                            return FileVisitResult.TERMINATE;
                        }
                    }
                });
        if (exception.get() instanceof IOException ioe) {
            throw ioe;
        }
        if (exceptionType.isInstance(exception.get())) {
            throw exceptionType.cast(exception.get());
        }
        Exceptions.rethrowIfAny(exception.get());
    }

    public static void delete(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        if (Files.isRegularFile(root)) {
            Files.delete(root);
            return;
        }
        try (var stream = Files.walk(root).sorted(Comparator.reverseOrder())) {
            for (var path : stream.toList()) {
                Files.delete(path);
            }
        }
    }

    public static boolean isEmpty(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (var stream = Files.newDirectoryStream(path)) {
                return !stream.iterator().hasNext();
            }
        }
        return Files.size(path) == 0;
    }

    public static List<Path> listFilesRecursively(Path directory) throws IOException {
        var files = new ArrayList<Path>();
        FileSystemUtil.visitDirsRecursively(
                directory,
                IOException.class,
                p -> {
                    files.addAll(listFiles(p));
                    return FileVisitResult.CONTINUE;
                });
        return files;
    }

    private FileSystemUtil() {}
}
