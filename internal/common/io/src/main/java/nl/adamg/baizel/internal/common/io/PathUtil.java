package nl.adamg.baizel.internal.common.io;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import javax.annotation.CheckForNull;

public final class PathUtil {
    private PathUtil() {}

    public static String getFileName(Path path) {
        return Objects.requireNonNullElse(path.getFileName(), path).toString();
    }

    public static String getFileNameWithoutExtension(Path path) {
        return getFileNameWithoutExtension(getFileName(path));
    }

    public static String getFileNameWithoutExtension(String fileName) {
        var dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            return fileName.substring(0, dotIndex);
        }
        return fileName;
    }

    @CheckForNull
    public static String getExtension(Path path) {
        return getExtension(getFileName(path));
    }

    /**
     * @return lower case
     */
    @CheckForNull
    public static String getExtension(String fileName) {
        var lowerCaseFileName = fileName.toLowerCase();
        var dotIndex = lowerCaseFileName.lastIndexOf('.');
        if (dotIndex < 0) {
            return null;
        }
        var extension = lowerCaseFileName.substring(dotIndex + 1);
        if (extension.isEmpty()) {
            return null;
        }
        return extension;
    }

    /** For path like '/a/b/c' returns [ '/', /a' and '/a/b' ] */
    public static List<Path> getAncestors(Path path) {
        return getAncestors(path, false);
    }

    public static List<Path> getAncestors(Path path, boolean includeSelf) {
        if (path.toString().isEmpty() || path.toString().equals("/")) {
            return includeSelf ? List.of(path) : List.of();
        }
        var chain = new ArrayList<Path>();
        var partialPath = Objects.requireNonNullElse(path.getRoot(), Path.of(""));
        chain.add(partialPath);
        for (var i = 0; i < path.getNameCount() - 1; i++) {
            partialPath = partialPath.resolve(path.getName(i));
            chain.add(partialPath);
        }
        if (includeSelf) {
            chain.add(path);
        }
        return chain;
    }

    public static Path getParent(Path path, int level) {
        for(int i=0; i<level; i++) {
            path = path.getParent();
        }
        return path;
    }

    public static String toUnixString(Path path) {
        var joiner = new StringJoiner("/");
        path.forEach(p -> joiner.add(p.toString()));
        return (path.isAbsolute() ? "/" : "") + joiner;
    }
}
