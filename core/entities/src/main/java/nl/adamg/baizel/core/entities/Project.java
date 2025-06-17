package nl.adamg.baizel.core.entities;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import nl.adamg.baizel.internal.common.annotations.Entity;

/// - API:    [nl.adamg.baizel.core.api.Project]
/// - Entity: [nl.adamg.baizel.core.entities.Project]
/// - Impl:   [nl.adamg.baizel.core.impl.ProjectImpl]
@SuppressWarnings("JavadocReference")
@Entity
public class Project implements Serializable {
    public String projectId;
    public String root;
    public Map<String, Module> modules;
    /// key: module ID, value: maven artifact coordinates
    public Map<String, ArtifactCoordinates> dependencies;
    public List<String> artifactRepositories;

    //region generated code
    public Project(
            String projectId,
            String root,
            Map<String, Module> modules,
            Map<String, ArtifactCoordinates> dependencies,
            List<String> artifactRepositories
    ) {
        this.projectId = projectId;
        this.root = root;
        this.modules = modules;
        this.dependencies = dependencies;
        this.artifactRepositories = artifactRepositories;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        Project project = (Project) object;
        return Objects.equals(projectId, project.projectId) && Objects.equals(root, project.root) && Objects.equals(modules, project.modules) && Objects.equals(dependencies, project.dependencies) && Objects.equals(artifactRepositories, project.artifactRepositories);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, root, modules, dependencies, artifactRepositories);
    }
    //endregion
}
