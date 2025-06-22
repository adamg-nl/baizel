package nl.adamg.baizel.core.api;

import java.io.IOException;
import nl.adamg.baizel.internal.common.annotations.ServiceProvider;
import nl.adamg.baizel.internal.common.io.FileSystem;
import nl.adamg.baizel.internal.common.io.Shell;

@ServiceProvider.Interface
public interface VersionTracker {
    SemanticVersion getVersion(Project project, Shell shell, FileSystem fileSystem) throws IOException;
}
