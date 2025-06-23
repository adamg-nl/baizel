package nl.adamg.baizel.core.api;

import java.io.IOException;
import javax.annotation.CheckForNull;
import java.nio.file.Path;
import java.util.List;

/// - API:    [nl.adamg.baizel.core.api.Project]
/// - Entity: [nl.adamg.baizel.core.entities.Project]
/// - Impl:   [nl.adamg.baizel.core.impl.ProjectImpl]
@SuppressWarnings("JavadocReference")
public interface Project {
    String groupId();
    String projectId();
    Path path(String... path);
    Path root();
    @CheckForNull Module getModuleOf(Path file);
    @CheckForNull Module getModuleById(String javaModuleId);
    @CheckForNull ArtifactCoordinates getArtifactCoordinates(String javaModuleId);
    List<String> artifactRepositories();
    SemanticVersion version() throws IOException;
    /// any extra fields from the `project-info.json` go here
    ObjectTree metadata();
    /// @throws BaizelException if module is not found or this target is not a module-type target
    Module getModule(TargetCoordinates coordinates) throws BaizelException;
}
