package nl.adamg.baizel.core.entities;

import java.io.Serializable;

/**
 * @see nl.adamg.baizel.core.Target
 */
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
