package nl.adamg.baizel.cli.tasks;

import com.sun.source.util.JavacTask;
import nl.adamg.baizel.core.entities.Issue;
import nl.adamg.baizel.core.api.Task;
import nl.adamg.baizel.core.model.Project;
import nl.adamg.baizel.core.model.Target;
import nl.adamg.baizel.core.model.TaskInput;
import nl.adamg.baizel.core.model.TaskRequest;
import nl.adamg.baizel.internal.bootstrap.util.collections.Items;
import nl.adamg.baizel.internal.common.annotations.ServiceProvider;
import nl.adamg.baizel.internal.common.util.LoggerUtil;

import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import static nl.adamg.baizel.internal.bootstrap.util.collections.Items.mapToList;
import static nl.adamg.baizel.internal.bootstrap.util.collections.Items.mergeSet;

public class Compile implements Task {
    private static final Logger LOG = Logger.getLogger(Compile.class.getName());
    public static final String TASK_ID = "compile";
    private final PrintStream logStream;
    private final JavaCompiler compiler;
    private final DiagnosticListener<JavaFileObject> diagnosticListener;
    private final StandardJavaFileManager fileManager;

    @ServiceProvider(Task.class)
    public Compile() {
        this.logStream = System.err;
        this.compiler = ToolProvider.getSystemJavaCompiler();
        this.diagnosticListener = logStream::println;
        this.fileManager = compiler.getStandardFileManager(diagnosticListener, null, null);
    }

    @Override
    public String getTaskId() {
        return TASK_ID;
    }

    @Override
    public Set<TaskRequest> findDependencies(Project project, Target target) throws IOException {
        var module = target.getModule(project);
        if (module == null) {
            return Set.of();
        }
        var dependencies = new TreeSet<TaskRequest>();
        for (var requirement : module.requirements()) {
            if (requirement.isSdkRequirement()) {
                continue;
            }
            var requiredModule = project.getModuleById(requirement.moduleId());
            if (requiredModule != null) {
                dependencies.add(new TaskRequest(Target.module(requiredModule.path()), TASK_ID));
                continue;
            }
            var requiredArtifact = project.getArtifactCoordinates(requirement.moduleId());
            if (requiredArtifact != null) {
                var artifactTarget = Target.artifact(requiredArtifact.organization(), requiredArtifact.artifact());
                dependencies.add(new TaskRequest(artifactTarget, Resolve.TASK_ID));
                continue;
            }
            project.reporter().accept(new Issue("UNRESOLVED_DEPENDENCY", Map.of("moduleId", requirement.moduleId())));
        }
        return dependencies;
    }

    @Override
    public Set<Path> run(Target target, List<String> args, List<TaskInput> inputs, Project project, Baizel baizel) {
        LOG.warning("compiling" + LoggerUtil.with("target", target.toString()));
        var module = target.getModule(project);
        if (module == null) {
            project.report("MODULE_NOT_FOUND", "path", target.path());
            return Set.of();
        }
        var sourceSet = target.getSourceSet();
        var sourceRoot = module.getSourceRoot(target.getSourceSet());
        var artifactRoots = new TreeSet<Path>();
        for(var input : inputs) {
            artifactRoots.add(null); // TODO
        }
        var outputDir = module.fullPath().resolve(".build/classes/java").resolve(sourceSet.getSourceSetId());
        compile(Set.of(sourceRoot), artifactRoots, outputDir);
        return Set.of();
    }

    private boolean compile(Set<Path> sourceRoots, Set<Path> artifactRoots, Path outputDir) throws IOException {
        var sourceFiles = mergeSet(mapToList(sourceRoots, Compile::findJavaSources));
        var compilationUnits = fileManager.getJavaFileObjectsFromFiles(sourceFiles.stream().map(Path::toFile).toList());
        var javacArgs = new ArrayList<>(List.of(
                "-d", outputDir.toString(),
                "-g", "-parameters", // extended metadata
                "-implicit:none"
        ));
        fileManager.setLocation(StandardLocation.CLASS_PATH, Items.mapToList(artifactRoots, Path::toFile));
        LOG.info("$ javac " + String.join(" ", javacArgs) + " ...");
        var task = (JavacTask) compiler.getTask(
                new PrintWriter(logStream),
                fileManager,
                diagnosticListener,
                javacArgs,
                null,
                compilationUnits
        );
        return task.call();
    }

    private static List<Path> findJavaSources(Path directory) throws IOException {
        try(var stream = Files.walk(directory)) {
            return stream.filter(p -> p.toString().endsWith(".java") && ! p.getFileName().toString().equals("module-info.java")).toList();
        }
    }

    //region generated code
    public Compile(
            PrintStream logStream,
            JavaCompiler compiler,
            DiagnosticListener<JavaFileObject> diagnosticListener,
            StandardJavaFileManager fileManager
    ) {
        this.logStream = logStream;
        this.compiler = compiler;
        this.diagnosticListener = diagnosticListener;
        this.fileManager = fileManager;
    }
    //endregion
}
