package nl.adamg.baizel.core.api;

import javax.annotation.CheckForNull;
import java.nio.file.Path;
import java.util.List;

/// - API:    [nl.adamg.baizel.core.api.Project]
/// - Entity: [nl.adamg.baizel.core.entities.Project]
/// - Model: [nl.adamg.baizel.core.impl.ProjectImpl]
public interface Project {
    String projectId();
    Path path(String... path);
    Path root();
    @CheckForNull Module getModuleOf(Path file);
    @CheckForNull Module getModuleById(String javaModuleId);
    @CheckForNull ArtifactCoordinates getArtifactCoordinates(String javaModuleId);
    List<String> artifactRepositories();
}
