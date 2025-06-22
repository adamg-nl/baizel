package nl.adamg.baizel.core.entities;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/// - API:    [nl.adamg.baizel.core.api.Module]
/// - Entity: [nl.adamg.baizel.core.entities.Module]
/// - Impl:   [nl.adamg.baizel.core.impl.ModuleImpl]
@SuppressWarnings("JavadocReference")
public class Module implements Serializable {
    /// module root relative to the project root
    public String path;
    /// key: source set path relative to module root
    public Map<String, SourceRoot> sourceRoots;
    /// qualified package names
    public List<String> exports;
    public List<Requirement> requirements;

    //region generated code
    public Module(
            String path,
            Map<String, SourceRoot> sourceRoots,
            List<String> exports,
            List<Requirement> requires
    ) {
        this.path = path;
        this.sourceRoots = sourceRoots;
        this.exports = exports;
        this.requirements = requires;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        Module module = (Module) object;
        return Objects.equals(path, module.path) && Objects.equals(sourceRoots, module.sourceRoots) && Objects.equals(exports, module.exports) && Objects.equals(requirements, module.requirements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, sourceRoots, exports, requirements);
    }
    //endregion
}
