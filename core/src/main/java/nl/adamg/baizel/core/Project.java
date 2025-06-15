package nl.adamg.baizel.core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class Project {
    private final nl.adamg.baizel.core.entities.Project entity;

    public static Path findProjectRoot(Path path) {
        var root = path;
        while (root != null && !Files.exists(root.resolve("project-info.java"))) {
            root = root.getParent();
        }
        // fall back to considering given directory as implicit project root
        return Objects.requireNonNullElse(root, path);
    }

    public static Project load(Path root) {
        return new Project(new nl.adamg.baizel.core.entities.Project(root));
    }

    //region generated code
    public Project(nl.adamg.baizel.core.entities.Project entity) {
        this.entity = entity;
    }
    //endregion
}
