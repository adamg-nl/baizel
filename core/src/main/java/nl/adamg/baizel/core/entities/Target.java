package nl.adamg.baizel.core.entities;

import javax.annotation.CheckForNull;
import java.io.Serializable;

/**
 * @see nl.adamg.baizel.core.Target
 */
public class Target implements Serializable {
    @CheckForNull public String organization;
    @CheckForNull public String artifact;
    @CheckForNull public String path;
    @CheckForNull public String targetName;

    //region generated code
    public Target(@CheckForNull String organization, @CheckForNull String artifact, String path, @CheckForNull String targetName) {
        this.organization = organization;
        this.artifact = artifact;
        this.path = path;
        this.targetName = targetName;
    }
    //endregion
}
