package nl.adamg.baizel.core;

import nl.adamg.baizel.core.entities.Issue;
import nl.adamg.baizel.internal.common.javadsl.JavaDslReader;
import nl.adamg.baizel.internal.common.util.EntityModel;

import javax.annotation.CheckForNull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

public class Module extends EntityModel<nl.adamg.baizel.core.entities.Module, Module> {
    private static final String MODULE_DEF_FILE_PATH = "src/main/java/module-info.java";
    private final Project project;
    private final Map<String, Class> classes = new TreeMap<>();
    private final AtomicBoolean moduleDefFileLoaded = new AtomicBoolean(false);
    private final List<Requirement> requirements = new ArrayList<>();
    private final Consumer<Issue> reporter;

    @CheckForNull
    public static Path getModuleDefinitionFile(Path module) {
        var file = module.resolve(MODULE_DEF_FILE_PATH);
        return Files.exists(file) ? file : null;
    }

    public static Module load(Project project, String path, Consumer<Issue> reporter) {
        var classEntities = new TreeMap<String, nl.adamg.baizel.core.entities.Class>();
        var moduleEntity = new nl.adamg.baizel.core.entities.Module(path, classEntities, new ArrayList<>(), new ArrayList<>());
        return new Module(project, moduleEntity, reporter);
    }

    //region getters
    public Path path() {
        return project.root().resolve(entity.path);
    }

    public Map<String, Class> classes() {
        return classes;
    }

    public List<Requirement> requirements() throws IOException {
        ensureModuleDefFileLoaded();
        return requirements;
    }

    public List<String> exports() throws IOException {
        ensureModuleDefFileLoaded();
        return entity.exports;
    }
    //endregion

    //region implementation internals
    private void ensureModuleDefFileLoaded() throws IOException {
        if (moduleDefFileLoaded.get()) {
            return;
        }
        synchronized (moduleDefFileLoaded) {
            if (! moduleDefFileLoaded.get()) {
                loadModuleDefFile();
                moduleDefFileLoaded.set(true);
            }
        }
    }

    private void loadModuleDefFile() throws IOException {
        var moduleDefPath = getModuleDefinitionFile(path());
        if (moduleDefPath == null) {
            return; // nothing to load
        }
        var moduleDef = JavaDslReader.read(moduleDefPath);
        for(var requirementEntry : moduleDef.body().get("requires").list()) {
            nl.adamg.baizel.core.entities.Requirement requirement;
            if (requirementEntry instanceof String moduleId) {
                requirement = new nl.adamg.baizel.core.entities.Requirement(moduleId, false);
            } else if (requirementEntry instanceof List<?> list && list.size() == 2 && "transitive".equals(list.get(0)) && (list.get(1) instanceof String moduleId)) {
                requirement = new nl.adamg.baizel.core.entities.Requirement(moduleId, true);
            } else {
                reporter.accept(new Issue("INVALID_REQUIREMENT", Map.of("code", String.valueOf(requirementEntry))));
                continue;
            }
            entity.requirements.add(requirement);
        }
        entity.exports.addAll(moduleDef.body().get("exports").list());
    }
    //endregion

    //region entity model
    @Override
    public String toString() {
        return entity.path;
    }

    @Override
    protected List<Function<nl.adamg.baizel.core.entities.Module, ?>> fields() {
        return List.of(
                m -> m.path,
                m -> m.classes
        );
    }
    //endregion

    //region generated code
    public Module(
            Project project,
            nl.adamg.baizel.core.entities.Module entity,
            Consumer<Issue> reporter
    ) {
        super(entity);
        this.project = project;
        this.reporter = reporter;
    }
    //endregion
}
