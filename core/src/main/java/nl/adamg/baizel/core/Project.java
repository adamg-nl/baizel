package nl.adamg.baizel.core;

import java.nio.file.Path;

public class Project {
    private final nl.adamg.baizel.core.entities.Project entity;

    public static Project findAndLoad(Path path) {
        return new Project(new nl.adamg.baizel.core.entities.Project());
    }

    //region generated code
    public Project(nl.adamg.baizel.core.entities.Project entity) {
        this.entity = entity;
    }
    //endregion
}
