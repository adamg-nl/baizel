package nl.adamg.baizel.core.entities;

import java.io.Serializable;
import java.util.Objects;

/// - API:    [nl.adamg.baizel.core.api.Target]
/// - Entity: [nl.adamg.baizel.core.entities.Target]
/// - Impl:   [nl.adamg.baizel.core.impl.TargetImpl]
@SuppressWarnings("JavadocReference")
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

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        Target target = (Target) object;
        return Objects.equals(organization, target.organization) && Objects.equals(artifact, target.artifact) && Objects.equals(path, target.path) && Objects.equals(targetName, target.targetName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organization, artifact, path, targetName);
    }
    //endregion
}
