package nl.adamg.baizel.core.entities;

import java.io.Serializable;
import java.util.Objects;
import nl.adamg.baizel.internal.common.annotations.Entity;

/// - API:    [nl.adamg.baizel.core.api.Requirement]
/// - Entity: [nl.adamg.baizel.core.entities.Requirement]
/// - Impl:   [nl.adamg.baizel.core.impl.RequirementImpl]
@SuppressWarnings("JavadocReference")
@Entity
public class Requirement implements Serializable {
    public String moduleId;
    public boolean isTransitive;

    //region generated code
    public Requirement(String moduleId, boolean isTransitive) {
        this.moduleId = moduleId;
        this.isTransitive = isTransitive;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        Requirement that = (Requirement) object;
        return isTransitive == that.isTransitive && Objects.equals(moduleId, that.moduleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(moduleId, isTransitive);
    }
    //endregion
}
