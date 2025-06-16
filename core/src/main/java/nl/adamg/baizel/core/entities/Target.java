package nl.adamg.baizel.core.entities;

import java.io.Serializable;

/// - API:    [nl.adamg.baizel.core.api.Target]
/// - Entity: [nl.adamg.baizel.core.entities.Target]
/// - Model:  [nl.adamg.baizel.core.model.Target]
public class Target implements Serializable {
    public String organization;
    public String artifact;
    public String path;
    public String targetName;

    //region generated code
    public Target(String organization, String artifact, String path, String targetName) {
        this.organization = organization;
        this.artifact = artifact;
        this.path = path;
        this.targetName = targetName;
    }
    //endregion
}
