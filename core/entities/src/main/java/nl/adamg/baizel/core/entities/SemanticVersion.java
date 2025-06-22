package nl.adamg.baizel.core.entities;

import java.io.Serializable;

public class SemanticVersion implements Serializable {
    public int major;
    public int minor;
    public int patch;
    public String prerelease;
    public String build;

    //region generated code
    public SemanticVersion(int major, int minor, int patch, String prerelease, String build) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.prerelease = prerelease;
        this.build = build;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
    //endregion
}
