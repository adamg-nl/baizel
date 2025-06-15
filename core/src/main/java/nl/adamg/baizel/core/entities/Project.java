package nl.adamg.baizel.core.entities;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.Map;

public class Project implements Serializable {
    public String root;
    public Map<String, Module> modules;

    //region generated code
    public Project(String root) {
        this.root = root;
    }
    //endregion
}
