package nl.adamg.baizel.core;

import java.nio.file.Path;

public class Project {
    private final nl.adamg.baizel.core.entities.Project entity;

    public static Path findProjectRoot(Path path) {
        return path; // fall back to considering given directory as implicit project root
    }

    public static Project load(Path path) {
        var root = path;
        return new Project(new nl.adamg.baizel.core.entities.Project());
    }

    //region generated code
    public Project(nl.adamg.baizel.core.entities.Project entity) {
        this.entity = entity;
    }
    //endregion
}
