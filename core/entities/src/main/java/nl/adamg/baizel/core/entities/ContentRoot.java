package nl.adamg.baizel.core.entities;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/// - API:    [nl.adamg.baizel.core.api.ContentRoot]
/// - Entity: [nl.adamg.baizel.core.entities.ContentRoot]
/// - Impl:   [nl.adamg.baizel.core.impl.ContentRootImpl]
@SuppressWarnings("JavadocReference")
public class ContentRoot implements Serializable {
    /// key: qualified class name
    public Map<String, Class> classes;

    //region generated code
    public ContentRoot(Map<String, Class> classes) {
        this.classes = classes;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        ContentRoot that = (ContentRoot) object;
        return Objects.equals(classes, that.classes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classes);
    }
    //endregion
}
