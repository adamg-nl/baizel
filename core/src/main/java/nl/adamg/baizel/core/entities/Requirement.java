package nl.adamg.baizel.core.entities;

import java.io.Serializable;

public class Requirement implements Serializable {
    public String moduleId;
    public boolean isTransitive;

    //region generated code
    public Requirement(String moduleId, boolean isTransitive) {
        this.moduleId = moduleId;
        this.isTransitive = isTransitive;
    }
    //endregion
}
