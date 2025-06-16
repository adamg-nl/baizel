package nl.adamg.baizel.core.model;

import nl.adamg.baizel.core.entities.Issue;
import nl.adamg.baizel.internal.bootstrap.util.collections.ObjectTree;
import nl.adamg.baizel.internal.common.javadsl.JavaDslReader;
import nl.adamg.baizel.internal.common.util.EntityModel;
import nl.adamg.baizel.internal.common.util.collections.Items;
import nl.adamg.baizel.internal.common.util.java.typeref.TypeRef2;

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
/// - Model:  [nl.adamg.baizel.core.model.Project]
public class Project extends EntityModel<nl.adamg.baizel.core.entities.Project, Project> implements nl.adamg.baizel.core.api.Project {
    private static final String PROJECT_DEF_FILE_NAME = "project-info.java";
    private final Map<String, nl.adamg.baizel.core.model.Module> modules = new TreeMap<>();
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
        var rawDependencies = projectDef.body().get("dependencies").body();
        for(var coordinatesString : rawDependencies.keys()) {
            var modulesForCoordinate = rawDependencies.get(coordinatesString).body().list(List.class);
            for(var moduleId : modulesForCoordinate) {
                var dependencyEntity = ArtifactCoordinates.parse(coordinatesString);
                dependencyEntity.moduleId = String.valueOf(moduleId.get(0));
                dependenciesEntity.put(dependencyEntity.moduleId, dependencyEntity);
                dependencies.put(dependencyEntity.moduleId, new ArtifactCoordinates(dependencyEntity));
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
    @Override
    public nl.adamg.baizel.core.model.Module getModuleOf(Path path) {
        if (! path.startsWith(root())) {
            return null;
        }
        var modulePath = path;
        while (nl.adamg.baizel.core.model.Module.getModuleDefinitionFile(modulePath) != null) {
            modulePath = modulePath.getParent();
            if (! modulePath.startsWith(root())) {
                return null;
            }
        }
        return getModule(root().relativize(path).toString());
    }

    public nl.adamg.baizel.core.model.Module getModule(String path) {
        return modules.computeIfAbsent(path, p -> nl.adamg.baizel.core.model.Module.load(this, p, reporter));
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
        return new ArtifactCoordinates(coordinates);
    }

    public void report(String issueId, String... details) {
        reporter.accept(new Issue(issueId, Items.map(new TypeRef2<>() {}, details)));
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
