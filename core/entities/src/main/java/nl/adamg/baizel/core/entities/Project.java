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
    /// Domain name of the organization that owns the project (example: `com.acme`)
    public String groupId;
    /// Domain name of this project, qualified with the name of owner organization (example: `com.acme.nuclear.kitchen`)
    public String projectId;
    /// absolute local path of the project root dir (example: `/home/projects/nuclear-kitchen`)
    public String root;
    /// key: module path relative to project root dir (example: `toaster/api`)
    public Map<String, Module> modules;
    /// key: module ID (example: `com.acme.nuclear.kitchen.toaster.api`),
    /// value: maven artifact coordinates (example: `com.acme.nuclear.kitchen:toaster-api`)
    public Map<String, ArtifactCoordinates> dependencies;
    public List<String> artifactRepositories;
    /// Any extra fields that were present at the top level of the project {} definition go here.
    /// For example: `license Copyright (C) 2025\\u003B` becomes entry `"license" -> "Copyright (C) 2025;"`
    public Map<String, Object> metadata;

    //region generated code
    public Project(
            String groupId, String projectId,
            String root,
            Map<String, Module> modules,
            Map<String, ArtifactCoordinates> dependencies,
            List<String> artifactRepositories,
            Map<String, Object> metadata
    ) {
        this.groupId = groupId;
        this.projectId = projectId;
        this.root = root;
        this.modules = modules;
        this.dependencies = dependencies;
        this.artifactRepositories = artifactRepositories;
        this.metadata = metadata;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        Project project = (Project) object;
        return Objects.equals(groupId, project.groupId) && Objects.equals(projectId, project.projectId) && Objects.equals(root, project.root) && Objects.equals(modules, project.modules) && Objects.equals(dependencies, project.dependencies) && Objects.equals(artifactRepositories, project.artifactRepositories);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, projectId, root, modules, dependencies, artifactRepositories);
    }
    //endregion
}
