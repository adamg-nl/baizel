package nl.adamg.baizel.core.entities;

import java.io.Serializable;
import java.util.Objects;
import nl.adamg.baizel.internal.common.annotations.Entity;

/// - API:    [nl.adamg.baizel.core.api.ArtifactCoordinates]
/// - Entity: [nl.adamg.baizel.core.entities.ArtifactCoordinates]
/// - Impl:   [nl.adamg.baizel.core.impl.ArtifactCoordinatesImpl]
@SuppressWarnings("JavadocReference")
@Entity
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

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        ArtifactCoordinates that = (ArtifactCoordinates) object;
        return Objects.equals(organization, that.organization) && Objects.equals(artifact, that.artifact) && Objects.equals(version, that.version) && Objects.equals(moduleId, that.moduleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organization, artifact, version, moduleId);
    }
    //endregion
}
