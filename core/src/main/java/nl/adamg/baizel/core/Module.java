package nl.adamg.baizel.core;

import nl.adamg.baizel.internal.common.javadsl.JavaDslReader;
import nl.adamg.baizel.internal.common.util.EntityModel;

import javax.annotation.CheckForNull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class Module extends EntityModel<nl.adamg.baizel.core.entities.Module, Module> {
    private static final String MODULE_DEF_FILE_PATH = "src/main/java/module-info.java";
    private final Project project;
    private final Map<String, Class> classes = new TreeMap<>();
    private final AtomicBoolean moduleDefFileLoaded = new AtomicBoolean(false);

    @CheckForNull
    public static Path getModuleDefinitionFile(Path module) {
        var file = module.resolve(MODULE_DEF_FILE_PATH);
        return Files.exists(file) ? file : null;
    }

    public static Module load(Project project, String path) {
        var classEntities = new TreeMap<String, nl.adamg.baizel.core.entities.Class>();
        var moduleEntity = new nl.adamg.baizel.core.entities.Module(path, classEntities, new TreeSet<>(), new TreeSet<>());
        return new Module(project, moduleEntity);
    }

    //region getters
    public Path path() {
        return project.root().resolve(entity.path);
    }

    public Map<String, Class> classes() {
        return classes;
    }

    public Set<String> requires() throws IOException {
        ensureModuleDefFileLoaded();
        return entity.requires;
    }

    public Set<String> exports() throws IOException {
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
        entity.requires.addAll(moduleDef.get("requires").list());
        entity.exports.addAll(moduleDef.get("exports").list());
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
            nl.adamg.baizel.core.entities.Module entity
    ) {
        super(entity);
        this.project = project;
    }
    //endregion
}
