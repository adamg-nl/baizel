package nl.adamg.baizel.core.impl;

import nl.adamg.baizel.core.api.Class;
import nl.adamg.baizel.core.api.Module;
import nl.adamg.baizel.core.api.Requirement;
import nl.adamg.baizel.core.api.SourceSet;
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

/// - API:    [nl.adamg.baizel.core.api.Module]
/// - Entity: [nl.adamg.baizel.core.entities.Module]
/// - Model: [nl.adamg.baizel.core.impl.ModuleImpl]
public class ModuleImpl
        extends EntityModel<Module, nl.adamg.baizel.core.entities.Module, ModuleImpl>
        implements Module {
    private static final String MODULE_DEF_FILE_PATH = "src/main/java/module-info.java";
    private final ProjectImpl project;
    private final Map<String, Class> classes;
    private final List<Requirement> requirements;
    private final AtomicBoolean moduleDefFileLoaded = new AtomicBoolean(false);
    private final Consumer<Issue> reporter;

    //region factory
    public static Module of(
            ProjectImpl project,
            Consumer<Issue> reporter,
            String path
    ) {
        return new ModuleImpl(
                project,
                reporter,
                new TreeMap<>(),
                new ArrayList<>(),
                new nl.adamg.baizel.core.entities.Module(
                        path,
                        new TreeMap<>(),
                        new ArrayList<>(),
                        new ArrayList<>()
                )
        );
    }
    //endregion

    @CheckForNull
    public static Path getModuleDefinitionFile(Path module) {
        var file = module.resolve(MODULE_DEF_FILE_PATH);
        if (Files.exists(file)) {
            return file;
        }
        if (module.endsWith(Path.of(MODULE_DEF_FILE_PATH).getParent())) {
            // if this dir is .../something/src/main/java, then even if it does contain module-info.java
            // this dir is not the module root but just a source root, the module root is 3 levels up
            return null;
        }
        file = module.resolve(Path.of(MODULE_DEF_FILE_PATH).getFileName().toString());
        if (Files.exists(file)) {
            // this dir contains /module-info.java, and does not end with .../src/main/java
            // it means it's a flat module with source root at the same path as module root
            return file;
        }
        return null;
    }

    @Override
    public Path fullPath() {
        return project.root().resolve(path());
    }

    /// @return null if this module does not have such a source root
    @CheckForNull
    @Override
    public Path getSourceRoot(SourceSet sourceSet) {
        var sourceRoot = fullPath().resolve(sourceSet.getPath());
        if (!Files.exists(sourceRoot)) {
            return null;
        }
        return sourceRoot;
    }

    //region getters
    @Override
    public String path() {
        return entity.path;
    }

    @Override
    public Map<String, Class> classes() {
        return classes;
    }

    @Override
    public List<Requirement> requirements() throws IOException {
        ensureModuleDefFileLoaded();
        return requirements;
    }

    @Override
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
        var moduleDefPath = getModuleDefinitionFile(fullPath());
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
            requirements.add(new RequirementImpl(requirement));
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
    public ModuleImpl(
            ProjectImpl project,
            Consumer<Issue> reporter,
            Map<String, Class> classes,
            List<Requirement> requirements,
            nl.adamg.baizel.core.entities.Module entity
    ) {
        super(entity);
        this.project = project;
        this.reporter = reporter;
        this.classes = classes;
        this.requirements = requirements;
    }
    //endregion
}
