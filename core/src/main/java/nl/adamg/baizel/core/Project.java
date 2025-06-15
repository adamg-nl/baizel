package nl.adamg.baizel.core;

import nl.adamg.baizel.internal.common.util.EntityModel;

import javax.annotation.CheckForNull;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;

public class Project extends EntityModel<nl.adamg.baizel.core.entities.Project, Project> {
    private final Map<String, Module> modules = new TreeMap<>();

    public static Path findProjectRoot(Path path) {
        var root = path;
        while (root != null && !Files.exists(root.resolve("project-info.java"))) {
            root = root.getParent();
        }
        // fall back to considering given directory as implicit project root
        return Objects.requireNonNullElse(root, path);
    }

    public static Project load(Path root) {
        return new Project(new nl.adamg.baizel.core.entities.Project(root.toAbsolutePath().toString()));
    }

    /**
     * @return null if path is not part of any project module
     */
    @CheckForNull
    public Module getModuleOf(Path path) {
        if (! path.startsWith(root())) {
            return null;
        }
        var modulePath = path;
        while (Module.getModuleFile(modulePath) != null) {
            modulePath = modulePath.getParent();
            if (! modulePath.startsWith(root())) {
                return null;
            }
        }
        return getModule(path);
    }

    public Module getModule(Path path) {
        var relativePath = root().relativize(path).toString();
        return modules.computeIfAbsent(relativePath, p -> loadModule(p));
    }

    private Module loadModule(String path) {
        return null;
    }

    //region getters
    public Path root() {
        return Path.of(entity.root);
    }
    //endregion

    //region entity model
    @Override
    public String toString() {
        return entity.root;
    }

    @Override
    protected List<Function<nl.adamg.baizel.core.entities.Project, ?>> fields() {
        return List.of(p -> p.root);
    }
    //endregion

    //region generated code
    public Project(nl.adamg.baizel.core.entities.Project entity) {
        super(entity);
    }
    //endregion
}
