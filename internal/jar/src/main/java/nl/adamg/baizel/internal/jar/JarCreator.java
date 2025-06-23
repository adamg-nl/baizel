package nl.adamg.baizel.internal.jar;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.Deflater;
import nl.adamg.baizel.internal.common.io.FileSystem;
import nl.adamg.baizel.internal.common.io.PathUtil;

public class JarCreator {
    private final FileSystem fileSystem;
    private final boolean setTimestamps;
    private final boolean useCompression;

    public void createJar(ArrayList<Path> inputRoots, Manifest manifest, Path outputJarPath) throws IOException {
        Files.createDirectories(outputJarPath.getParent());
        try (var jar = new JarOutputStream(fileSystem.newOutputStream(outputJarPath), manifest)) {
            jar.setLevel(useCompression ? Deflater.BEST_COMPRESSION : Deflater.NO_COMPRESSION);
            for(var inputRoot : inputRoots) {
                if (! fileSystem.exists(inputRoot)) {
                    throw new FileNotFoundException("invalid root provided: " + inputRoot);
                }
                var inputFiles = fileSystem.findFiles(inputRoot, ".*");
                for (var inputFile : inputFiles) {
                    try (var fileStream = fileSystem.newInputStream(inputFile)) {
                        jar.putNextEntry(createJarEntry(inputRoot, inputFile));
                        fileStream.transferTo(jar);
                        jar.closeEntry();
                    }
                }
            }
        }
    }

    private JarEntry createJarEntry(Path inputRootDir, Path inputFile) throws IOException {
        var entry = new JarEntry(PathUtil.toUnixString(inputRootDir.relativize(inputFile)));
        if (setTimestamps) {
            var attrs = Files.readAttributes(inputFile, BasicFileAttributes.class);
            entry.setTime(attrs.lastModifiedTime().toMillis());
        }
        return entry;
    }

    //region generated code
    public JarCreator(FileSystem fileSystem, boolean setTimestamps, boolean useCompression) {
        this.fileSystem = fileSystem;
        this.setTimestamps = setTimestamps;
        this.useCompression = useCompression;
    }
    //endregion
}
