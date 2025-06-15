package nl.adamg.baizel.core.entities;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.Map;

public class Project implements Serializable {
    public String projectId;
    public String root;
    public Map<String, Module> modules;
    /// key: module ID, value: maven artifact coordinates
    public Map<String, ArtifactCoordinates> dependencies;

    //region generated code
    public Project(
            String projectId,
            String root,
            Map<String, Module> modules,
            Map<String, ArtifactCoordinates> dependencies
    ) {
        this.projectId = projectId;
        this.root = root;
        this.modules = modules;
        this.dependencies = dependencies;
    }
    //endregion
}
