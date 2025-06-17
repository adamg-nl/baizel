package nl.adamg.baizel.core.impl;

import nl.adamg.baizel.core.api.Project;
import nl.adamg.baizel.core.api.ArtifactCoordinates;
import nl.adamg.baizel.core.api.Module;
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

/// - API:    [nl.adamg.baizel.core.api.Project]
/// - Entity: [nl.adamg.baizel.core.entities.Project]
/// - Model:  [nl.adamg.baizel.core.impl.ProjectImpl]
public class ProjectImpl
        extends EntityModel<Project, nl.adamg.baizel.core.entities.Project, ProjectImpl>
        implements Project {
    private static final String PROJECT_DEF_FILE_NAME = "project-info.java";
    private final Map<String, Module> modules = new TreeMap<>();
    private final Map<String, ArtifactCoordinates> dependencies;
    private final Consumer<Issue> reporter;

    //region factory
    public static Project of(
            Consumer<Issue> reporter,
            Map<String, ArtifactCoordinates> dependencies,
            String projectId,
            String root,
            Map<String, nl.adamg.baizel.core.entities.ArtifactCoordinates> dependencyEntities,
            List<String> artifactRepositories
    ) {
        return new ProjectImpl(
                reporter,
                dependencies,
                new nl.adamg.baizel.core.entities.Project(
                        projectId,
                        root,
                        new TreeMap<>(),
                        dependencyEntities,
                        artifactRepositories
                )
        );
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
        var rawDependencies = projectDef.body().get("dependencies").body();
        var artifactRepositories = projectDef.body().get("repository").list(String.class);
        for(var coordinatesString : rawDependencies.keys()) {
            var modulesForCoordinate = rawDependencies.get(coordinatesString).body().list(List.class);
            for(var moduleId : modulesForCoordinate) {
                var dependencyEntity = ((ArtifactCoordinatesImpl)ArtifactCoordinatesImpl.parse(coordinatesString)).entity();
                dependencyEntity.moduleId = String.valueOf(moduleId.get(0));
                dependenciesEntity.put(dependencyEntity.moduleId, dependencyEntity);
                dependencies.put(dependencyEntity.moduleId, new ArtifactCoordinatesImpl(dependencyEntity));
            }
        }
        return ProjectImpl.of(
                reporter,
                dependencies,
                projectId,
                root.toAbsolutePath().toString(),
                dependenciesEntity,
                artifactRepositories
        );
    }
    //endregion

    public static Path findProjectRoot(Path path) {
        var root = path;
        while (root != null && getProjectDefinitionFile(root) == null) {
            root = root.getParent();
        }
        // fall back to considering given directory as implicit project root
        return Objects.requireNonNullElse(root, path);
    }

    /**
     * @return null if path is not part of any project module
     */
    @CheckForNull
    @Override
    public Module getModuleOf(Path path) {
        if (! path.startsWith(root())) {
            return null;
        }
        var modulePath = path;
        while (ModuleImpl.getModuleDefinitionFile(modulePath) != null) {
            modulePath = modulePath.getParent();
            if (! modulePath.startsWith(root())) {
                return null;
            }
        }
        return getModule(root().relativize(path).toString());
    }

    public Module getModule(String path) {
        return modules.computeIfAbsent(path, p -> ModuleImpl.of(this, reporter, p));
    }

    /// @param moduleId qualified Java module id, like `com.example.foobar`
    @CheckForNull
    @Override
    public Module getModuleById(String moduleId) {
        if (! moduleId.startsWith(projectId())) {
            return null;
        }
        var path = moduleId.substring(projectId().length()+1).replace('.', '/');
        return getModule(path);
    }

    @CheckForNull
    @Override
    public ArtifactCoordinates getArtifactCoordinates(String moduleId) {
        var coordinates = entity.dependencies.get(moduleId);
        if (coordinates == null) {
            return null;
        }
        return new ArtifactCoordinatesImpl(coordinates);
    }

    @Override
    public List<String> artifactRepositories() {
        return entity.artifactRepositories;
    }

    /// @return given path, resolved relative to the root
    @Override
    public Path path(String... path) {
        var result = root();
        for(var segment : path) {
            result = result.resolve(segment);
        }
        return result;
    }

    //region implementation internals
    @CheckForNull
    private static Path getProjectDefinitionFile(Path projectRoot) {
        var file = projectRoot.resolve(PROJECT_DEF_FILE_NAME);
        return Files.exists(file) ? file : null;
    }
    //endregion

    //region getters
    @Override
    public Path root() {
        return Path.of(entity.root);
    }

    @Override
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
    public ProjectImpl(
            Consumer<Issue> reporter,
            Map<String, ArtifactCoordinates> dependencies,
            nl.adamg.baizel.core.entities.Project entity
    ) {
        super(entity);
        this.reporter = reporter;
        this.dependencies = dependencies;
    }
    //endregion
}
