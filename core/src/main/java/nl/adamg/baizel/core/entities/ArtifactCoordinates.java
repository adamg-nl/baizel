package nl.adamg.baizel.core.entities;

import javax.annotation.CheckForNull;
import java.io.Serializable;

public class ArtifactCoordinates implements Serializable {
    public String organization;
    public String artifact;
    public String version;
    @CheckForNull
    public String moduleId;

    //region generated code
    public ArtifactCoordinates(
            String organization,
            String artifact,
            String version,
            @CheckForNull String moduleId
    ) {
        this.organization = organization;
        this.artifact = artifact;
        this.version = version;
        this.moduleId = moduleId;
    }
    //endregion
}
