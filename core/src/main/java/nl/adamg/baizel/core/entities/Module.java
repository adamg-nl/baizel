package nl.adamg.baizel.core.entities;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class Module implements Serializable {
    public String path;
    public Map<String, Class> classes;
    public Set<String> exports;
    public Set<String> requires;

    //region generated code
    public Module(
            String path,
            Map<String, Class> classes,
            Set<String> exports,
            Set<String> requires
    ) {
        this.path = path;
        this.classes = classes;
        this.exports = exports;
        this.requires = requires;
    }
    //endregion
}
