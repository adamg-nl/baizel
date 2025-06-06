package nl.adamg.baizel.internal.common.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;

public final class ZipUtil {
    public static void unzip(Path inputZip, Path outputDir) throws IOException {
        if (Files.notExists(outputDir)) {
            Files.createDirectories(outputDir);
        }
        try (var zipFile = new ZipFile(inputZip.toFile())) {
            for (var entry : zipFile.stream().toList()) {
                var outputEntry = outputDir.resolve(entry.getName()).toAbsolutePath().normalize();
                if (!outputEntry.startsWith(outputDir)) {
                    throw new IOException(
                            "zip entry outside of zip root found (possibly malicious): '"
                                    + entry.getName()
                                    + "' in '"
                                    + inputZip
                                    + "'");
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(outputEntry);
                    continue;
                }
                Files.createDirectories(outputEntry.getParent());
                try (var in = zipFile.getInputStream(entry);
                        var out = Files.newOutputStream(outputEntry)) {
                    in.transferTo(out);
                }
            }
        }
    }

    private ZipUtil() {}
}
