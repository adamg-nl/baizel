package nl.adamg.baizel.core.entities;

import java.io.Serializable;
import java.util.Map;

public class Module implements Serializable {
    public String path;
    public Map<String, Class> classes;

    //region generated code
    public Module(String path, Map<String, Class> classes) {
        this.path = path;
        this.classes = classes;
    }
    //endregion
}
