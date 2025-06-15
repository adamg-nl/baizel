package nl.adamg.baizel.core.entities;

import java.io.Serializable;
import java.nio.file.Path;

public class Project implements Serializable {
    public Path root;

    //region generated code
    public Project(Path root) {
        this.root = root;
    }
    //endregion
}
