package nl.adamg.baizel.core;

import nl.adamg.baizel.core.entities.Issue;
import nl.adamg.baizel.internal.bootstrap.util.collections.ObjectTree;
import nl.adamg.baizel.internal.common.javadsl.JavaDslReader;
import nl.adamg.baizel.internal.common.util.EntityModel;

import javax.annotation.CheckForNull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class Project extends EntityModel<nl.adamg.baizel.core.entities.Project, Project> {
    private static final String PROJECT_DEF_FILE_NAME = "project-info.java";
    private final Map<String, Module> modules = new TreeMap<>();
    private final Consumer<Issue> reporter;
    private final Map<String, ArtifactCoordinates> dependencies;

    public static Path findProjectRoot(Path path) {
        var root = path;
        while (root != null && getProjectDefinitionFile(root) == null) {
            root = root.getParent();
        }
        // fall back to considering given directory as implicit project root
        return Objects.requireNonNullElse(root, path);
    }

    public static Project load(Path root, Consumer<Issue> reporter) throws IOException {
        var projectDefPath = getProjectDefinitionFile(root);
        ObjectTree projectDef;
        if (projectDefPath != null) {
            projectDef = JavaDslReader.read(projectDefPath);
            if (!"project".equals(projectDef.get(0).string())) {
                reporter.accept(new Issue("INVALID_PROJECT_FILE", Map.of("path", projectDefPath.toString())));
            }
        } else {
            projectDef = ObjectTree.of(List.of("project", root.getFileName().toString(), List.of()));
        }
        var projectId = projectDef.get(1).string();
        var dependenciesEntity = new TreeMap<String, nl.adamg.baizel.core.entities.ArtifactCoordinates>();
        var dependencies = new TreeMap<String, ArtifactCoordinates>();
        for(var coordinatesString : projectDef.body().keys()) {
            for(var moduleId : projectDef.body().get(coordinatesString).list(String.class)) {
                var dependencyEntity = ArtifactCoordinates.parse(coordinatesString);
                dependencyEntity.moduleId = moduleId;
                dependenciesEntity.put(moduleId, dependencyEntity);
                dependencies.put(moduleId, new ArtifactCoordinates(dependencyEntity));
            }
        }
        var entity = new nl.adamg.baizel.core.entities.Project(
                projectId,
                root.toAbsolutePath().toString(),
                new TreeMap<>(),
                dependenciesEntity
        );
        return new Project(entity, reporter, dependencies);
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
        while (Module.getModuleDefinitionFile(modulePath) != null) {
            modulePath = modulePath.getParent();
            if (! modulePath.startsWith(root())) {
                return null;
            }
        }
        return getModule(root().relativize(path).toString());
    }

    public Module getModule(String path) {
        return modules.computeIfAbsent(path, p -> Module.load(this, p, reporter));
    }

    /// @param moduleId qualified Java module id, like `com.example.foobar`
    @CheckForNull
    public Module getModuleById(String moduleId) {
        if (! moduleId.startsWith(projectId())) {
            return null;
        }
        var path = moduleId.substring(projectId().length()+1).replace('.', '/');
        return getModule(path);
    }

    @CheckForNull
    public ArtifactCoordinates getArtifactCoordinates(String moduleId) {
        var coordinates = entity.dependencies.get(moduleId);
        if (coordinates == null) {
            return null;
        }
        return new ArtifactCoordinates(coordinates);
    }

    //region implementation internals
    @CheckForNull
    private static Path getProjectDefinitionFile(Path projectRoot) {
        var file = projectRoot.resolve(PROJECT_DEF_FILE_NAME);
        return Files.exists(file) ? file : null;
    }
    //endregion

    //region getters
    public Path root() {
        return Path.of(entity.root);
    }

    public String projectId() {
        return entity.projectId;
    }

    public Consumer<Issue> reporter() {
        return reporter;
    }
    //endregion

    //region entity model
    @Override
    public String toString() {
        return entity.root;
    }

    @Override
    protected List<Function<nl.adamg.baizel.core.entities.Project, ?>> fields() {
        return List.of(p -> p.root, p -> p.projectId, p -> p.modules);
    }
    //endregion

    //region generated code
    public Project(
            nl.adamg.baizel.core.entities.Project entity,
            Consumer<Issue> reporter,
            Map<String, ArtifactCoordinates> dependencies
    ) {
        super(entity);
        this.reporter = reporter;
        this.dependencies = dependencies;
    }
    //endregion
}
