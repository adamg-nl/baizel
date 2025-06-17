package nl.adamg.baizel.core.entities;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import nl.adamg.baizel.internal.common.annotations.Entity;

/// - API:    [nl.adamg.baizel.core.api.Module]
/// - Entity: [nl.adamg.baizel.core.entities.Module]
/// - Impl:   [nl.adamg.baizel.core.impl.ModuleImpl]
@SuppressWarnings("JavadocReference")
@Entity
public class Module implements Serializable {
    public String path;
    public Map<String, Class> classes;
    public List<String> exports;
    public List<Requirement> requirements;

    //region generated code
    public Module(
            String path,
            Map<String, Class> classes,
            List<String> exports,
            List<Requirement> requires
    ) {
        this.path = path;
        this.classes = classes;
        this.exports = exports;
        this.requirements = requires;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        Module module = (Module) object;
        return Objects.equals(path, module.path) && Objects.equals(classes, module.classes) && Objects.equals(exports, module.exports) && Objects.equals(requirements, module.requirements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, classes, exports, requirements);
    }
    //endregion
}
