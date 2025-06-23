package nl.adamg.baizel.core.entities;

import java.io.Serializable;
import java.util.Objects;

/// - API:    [nl.adamg.baizel.core.api.TargetCoordinates]
/// - Entity: [nl.adamg.baizel.core.entities.TargetCoordinates]
/// - Impl:   [nl.adamg.baizel.core.impl.TargetCoordinatesImpl]
@SuppressWarnings("JavadocReference")
public class TargetCoordinates implements Serializable {
    public String organization;
    public String artifact;
    public String path;
    public String targetId;

    //region generated code
    public TargetCoordinates(String organization, String artifact, String path, String targetId) {
        this.organization = organization;
        this.artifact = artifact;
        this.path = path;
        this.targetId = targetId;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        TargetCoordinates target = (TargetCoordinates) object;
        return Objects.equals(organization, target.organization) && Objects.equals(artifact, target.artifact) && Objects.equals(path, target.path) && Objects.equals(targetId, target.targetId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organization, artifact, path, targetId);
    }
    //endregion
}
