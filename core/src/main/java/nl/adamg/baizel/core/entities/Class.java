package nl.adamg.baizel.core.entities;

import java.io.Serializable;
import java.util.List;

/// - API:    [nl.adamg.baizel.core.api.Class]
/// - Entity: [nl.adamg.baizel.core.entities.Class]
/// - Model: [nl.adamg.baizel.core.impl.ClassImpl]
public class Class implements Serializable {
    public String canonicalName;
    public List<String> imports;

    //region generated code
    public Class(String canonicalName, List<String> imports) {
        this.canonicalName = canonicalName;
        this.imports = imports;
    }
    //endregion
}
