package nl.adamg.baizel.internal.jar;

import java.nio.file.Path;
import java.util.List;
import javax.annotation.CheckForNull;

public record JarDefinition(
        List<Path> inputRootDir,
        Path outputJarPath,
        @CheckForNull String mainClass,
        String groupId,
        String artifactId,
        String moduleId,
        String version,
        String vendor
) {
}
