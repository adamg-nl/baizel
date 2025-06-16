package nl.adamg.baizel.core.entities;

import java.io.Serializable;

/// - API:    [nl.adamg.baizel.core.api.Requirement]
/// - Entity: [nl.adamg.baizel.core.entities.Requirement]
/// - Model: [nl.adamg.baizel.core.impl.RequirementImpl]
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
