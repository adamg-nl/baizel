package nl.adamg.baizel.internal.bootstrap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Set;

class Checksum {
    /**
     * Fast hashing function for directory, for detecting changes. Only checks file
     * metadata (timestamp, size, full path), not the content.
     */
    static String directory(Path startDir) throws IOException {
        var ignoredDirs = Set.of("build", "dist", "out", "node_modules");
        MessageDigest hasher;
        try {
            hasher = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        var fileVisitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                var name = dir.getFileName().toString();
                if (ignoredDirs.contains(name) || name.startsWith(".")) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (attrs.isRegularFile() && !file.getFileName().toString().startsWith(".")) {
                    hasher.update(longToByte(attrs.size()));
                    hasher.update(longToByte(attrs.lastModifiedTime().toMillis()));
                    hasher.update(startDir.relativize(file).toString().getBytes(StandardCharsets.UTF_8));
                }
                return FileVisitResult.CONTINUE;
            }
        };
        Files.walkFileTree(startDir, fileVisitor);
        return Base64.getEncoder().encodeToString(hasher.digest());
    }

    private static byte[] longToByte(long l) {
        return new byte[]{
                (byte) (l >>> 56),
                (byte) (l >>> 48),
                (byte) (l >>> 40),
                (byte) (l >>> 32),
                (byte) (l >>> 24),
                (byte) (l >>> 16),
                (byte) (l >>> 8),
                (byte) l
        };
    }
}
