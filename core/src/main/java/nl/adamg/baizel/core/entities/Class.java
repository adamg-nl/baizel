package nl.adamg.baizel.core.entities;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/// - API:    [nl.adamg.baizel.core.api.Class]
/// - Entity: [nl.adamg.baizel.core.entities.Class]
/// - Impl:   [nl.adamg.baizel.core.impl.ClassImpl]
public class Class implements Serializable {
    public String canonicalName;
    public List<String> imports;

    //region generated code
    public Class(String canonicalName, List<String> imports) {
        this.canonicalName = canonicalName;
        this.imports = imports;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        Class aClass = (Class) object;
        return Objects.equals(canonicalName, aClass.canonicalName) && Objects.equals(imports, aClass.imports);
    }

    @Override
    public int hashCode() {
        return Objects.hash(canonicalName, imports);
    }
    //endregion
}
