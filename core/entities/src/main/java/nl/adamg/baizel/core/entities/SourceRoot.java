package nl.adamg.baizel.core.entities;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/// - API:    [nl.adamg.baizel.core.api.SourceRoot]
/// - Entity: [nl.adamg.baizel.core.entities.SourceRoot]
/// - Impl:   [nl.adamg.baizel.core.impl.SourceRootImpl]
@SuppressWarnings("JavadocReference")
public class SourceRoot implements Serializable {
    /// source set root relative to the module root
    public String path;
    /// key: qualified class name
    public Map<String, Class> classes;

    //region generated code
    public SourceRoot(String path, Map<String, Class> classes) {
        this.path = path;
        this.classes = classes;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        SourceRoot that = (SourceRoot) object;
        return Objects.equals(path, that.path) && Objects.equals(classes, that.classes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, classes);
    }
    //endregion
}
