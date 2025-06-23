package nl.adamg.baizel.core.impl;

import java.util.Collection;
import java.util.regex.Pattern;
import nl.adamg.baizel.core.api.Class;
import nl.adamg.baizel.core.api.Module;
import nl.adamg.baizel.core.api.Project;
import nl.adamg.baizel.core.api.Requirement;
import nl.adamg.baizel.core.api.ContentRoot;
import nl.adamg.baizel.core.api.Target;
import nl.adamg.baizel.core.entities.BaizelErrors;
import nl.adamg.baizel.core.entities.Issue;
import nl.adamg.baizel.internal.common.io.FileSystem;
import nl.adamg.baizel.internal.common.io.Shell;
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
import nl.adamg.baizel.internal.common.util.Lazy;
import nl.adamg.baizel.internal.common.util.Text;

/// - API:    [nl.adamg.baizel.core.api.Module]
/// - Entity: [nl.adamg.baizel.core.entities.Module]
/// - Impl:   [nl.adamg.baizel.core.impl.ModuleImpl]
public class ModuleImpl
        extends EntityModel<nl.adamg.baizel.core.entities.Module>
        implements Module {
    private static final String MODULE_DEF_FILE_PATH = "src/main/java/module-info.java";
    private static final Pattern ENTRY_POINT_PATTERN = Pattern.compile(".*( |\"|'|^)(?<PATH>[^ \"']+.java)(\"|'| |$)");
    /// key: source root path relative to the module root
    private final Map<String, ContentRoot> contentRoots = new TreeMap<>();
    private final List<Requirement> requirements = new ArrayList<>();
    private final AtomicBoolean moduleDefFileLoaded = new AtomicBoolean(false);
    private final Lazy<Class, IOException> mainClass = new Lazy<>(this::findMainClass);
    private final Lazy.NonNull<ModuleDoc, IOException> moduleDoc = new Lazy.NonNull<>(this::readDoc);
    private final ProjectImpl project;
    private final Shell shell;
    private final FileSystem fileSystem;
    private final Consumer<Issue> reporter;

    //region factory
    public static Module of(
            ProjectImpl project,
            Shell shell,
            FileSystem fileSystem,
            Consumer<Issue> reporter,
            String path
    ) {
        return new ModuleImpl(
                project,
                shell,
                fileSystem,
                reporter,
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

    @Override
    @CheckForNull
    public Class mainClass() throws IOException {
        return mainClass.get();
    }

    @Override
    public Path buildDir() {
        return fullPath().resolve(".build");
    }

    @Override
    public String shortDescription() throws IOException {
        return moduleDoc.get().shortDescription();
    }

    @Override
    public String title() throws IOException {
        return moduleDoc.get().title();
    }

    @Override
    @CheckForNull
    public Class getClassByPath(String relativePath) {
        var targetType = Targets.getTarget(relativePath);
        if (targetType == null) {
            return null;
        }
        var target = getContentRoot(targetType);
        if (target == null) {
            return null;
        }
        var sourceRootRelativePath = relativePath.substring(targetType.contentRoot().length());
        var className = ClassImpl.sourcePathToClassName(sourceRootRelativePath);
        return target.getClass(className);
    }

    @CheckForNull
    @Override
    public ContentRoot getContentRoot(Target target) {
        return contentRoots.computeIfAbsent(target.contentRoot(), this::loadContentRoot);
    }

    @CheckForNull
    private ContentRoot loadContentRoot(String relativePath) {
        if (! fileSystem.exists(fullPath().resolve(relativePath))) {
            return null;
        }
        return ContentRootImpl.of(this, Targets.byContentRoot(relativePath), fileSystem);
    }

    @Override
    public Collection<ContentRoot> getAllContentRoots() {
        if (contentRoots.size() == Targets.values().size()) {
            return contentRoots.values();
        }
        var targets = new ArrayList<ContentRoot>();
        for(var type : Targets.values()) {
            var target = getContentRoot(type);
            if (target != null) {
                targets.add(target);
            }
        }
        return targets;
    }

    //region getters
    @Override
    public String path() {
        return entity.path;
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

    @Override
    public Project project() {
        return project;
    }

    @Override
    public String artifactId() {
        if (! project.projectId().startsWith(project.groupId()) || project.groupId().isEmpty() || project.groupId().equals(project.projectId())) {
            return Text.dashed(path());
        }
        var projectShortId = project.projectId().substring(project.groupId().length() + 1);
        return Text.dashed(projectShortId + "-" + path());
    }

    @Override
    public String groupId() {
        return project.projectId();
    }

    @Override
    public String moduleId() {
        return Text.dotted(project.projectId() + "." + path());
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
                reporter.accept(new Issue(
                        "INVALID_REQUIREMENT",
                        BaizelErrors.INPUT_ISSUE.exitCode,
                        Map.of("code", String.valueOf(requirementEntry)),
                        "Invalid requirement: ${code}"));
                continue;
            }
            entity.requirements.add(requirement);
            requirements.add(new RequirementImpl(requirement));
        }
        entity.exports.addAll(moduleDef.body().get("exports").list());
    }

    @CheckForNull
    private Class findMainClass() throws IOException {
        var shellEntryPoint = fullPath().resolve("main");
        if (! fileSystem.exists(shellEntryPoint)) {
            return null;
        }
        var lines = fileSystem.readAllLines(shellEntryPoint);
        for(var line : lines) {
            var matcher = ENTRY_POINT_PATTERN.matcher(line);
            if(matcher.matches()) {
                var path = matcher.group("PATH");
                if (path != null && Files.exists(project.path(path))) {
                    var relativePath = fullPath().relativize(project.path(path)).toString();
                    return getClassByPath(relativePath);
                }
            }
        }
        return null;
    }

    private ModuleDoc readDoc() throws IOException {
        return ModuleDoc.read(fullPath(), this.fileSystem);
    }
    //endregion

    //region entity model
    @Override
    public String toString() {
        return entity.path;
    }
    //endregion

    //region generated code
    public ModuleImpl(
            ProjectImpl project,
            Shell shell,
            FileSystem fileSystem,
            Consumer<Issue> reporter,
            nl.adamg.baizel.core.entities.Module entity
    ) {
        super(entity);
        this.project = project;
        this.reporter = reporter;
        this.shell = shell;
        this.fileSystem = fileSystem;
    }
    //endregion
}
