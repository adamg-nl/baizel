package nl.adamg.baizel.core.entities;

import java.io.Serializable;

/// - API:    [nl.adamg.baizel.core.api.ArtifactCoordinates]
/// - Entity: [nl.adamg.baizel.core.entities.ArtifactCoordinates]
/// - Model: [nl.adamg.baizel.core.impl.ArtifactCoordinatesImpl]
public class ArtifactCoordinates implements Serializable {
    public String organization;
    public String artifact;
    public String version;
    public String moduleId;

    //region generated code
    public ArtifactCoordinates(
            String organization,
            String artifact,
            String version,
            String moduleId
    ) {
        this.organization = organization;
        this.artifact = artifact;
        this.version = version;
        this.moduleId = moduleId;
    }
    //endregion
}
