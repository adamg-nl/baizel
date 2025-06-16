package nl.adamg.baizel.core.entities;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/// - API:    [nl.adamg.baizel.core.api.Module]
/// - Entity: [nl.adamg.baizel.core.entities.Module]
/// - Model: [nl.adamg.baizel.core.impl.ModuleImpl]
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
    //endregion
}
