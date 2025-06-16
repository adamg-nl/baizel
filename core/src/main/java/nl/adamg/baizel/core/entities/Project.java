package nl.adamg.baizel.core.entities;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/// - API:    [nl.adamg.baizel.core.api.Project]
/// - Entity: [nl.adamg.baizel.core.entities.Project]
/// - Model: [nl.adamg.baizel.core.impl.ProjectImpl]
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
    //endregion
}
