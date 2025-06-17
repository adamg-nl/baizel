package nl.adamg.baizel.cli.tasks;

import com.sun.source.util.JavacTask;
import nl.adamg.baizel.core.BaizelException;
import nl.adamg.baizel.core.api.Baizel;
import nl.adamg.baizel.core.api.Target;
import nl.adamg.baizel.core.api.Target.Type;
import nl.adamg.baizel.core.api.Task;
import nl.adamg.baizel.core.api.TaskInput;
import nl.adamg.baizel.core.api.TaskRequest;
import nl.adamg.baizel.core.entities.BaizelErrors;
import nl.adamg.baizel.core.entities.Issue;
import nl.adamg.baizel.core.impl.TargetImpl;
import nl.adamg.baizel.core.impl.TaskRequestImpl;
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
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
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
    public boolean isApplicable(Target target, Type targetType, Baizel baizel) {
        return targetType == Type.MODULE;
    }

    @Override
    public Set<TaskRequest> findDependencies(Target target, Type targetType, Baizel baizel) throws IOException {
        var module = target.getModule(baizel.project());
        var dependencies = new TreeSet<TaskRequest>();
        for (var requirement : module.requirements()) {
            if (requirement.isSdkRequirement()) {
                continue;
            }
            var requiredModule = baizel.project().getModuleById(requirement.moduleId());
            if (requiredModule != null) {
                dependencies.add(TaskRequestImpl.of(TargetImpl.module(requiredModule.path()), TASK_ID));
                continue;
            }
            var requiredArtifact = baizel.project().getArtifactCoordinates(requirement.moduleId());
            if (requiredArtifact != null) {
                var artifactTarget = TargetImpl.artifact(requiredArtifact.organization(), requirement.moduleId());
                dependencies.add(TaskRequestImpl.of(artifactTarget, Resolve.TASK_ID));
                continue;
            }
            baizel.report("UNRESOLVED_DEPENDENCY", "moduleId", requirement.moduleId());
        }
        return dependencies;
    }

    @Override
    public Set<Path> run(Target target, List<String> args, List<TaskInput> inputs, Type targetType, Baizel baizel) throws IOException {
        LOG.warning("compiling" + LoggerUtil.with("target", target.toString()));
        var module = target.getModule(baizel.project());
        var sourceSet = target.sourceSet();
        var sourceRoot = module.getSourceRoot(target.sourceSet());
        if (sourceRoot == null) {
            return Set.of(); // nothing to compile
        }
        var artifactRoots = new TreeSet<Path>();
        for(var input : inputs) {
            artifactRoots.addAll(input.paths());
        }
        var outputDir = module.fullPath().resolve(".build/classes/java").resolve(sourceSet.getSourceSetId());
        Files.createDirectories(outputDir);
        var succeeded = compile(Set.of(sourceRoot), artifactRoots, outputDir, baizel.reporter());
        if (! succeeded) {
            throw new BaizelException(BaizelErrors.TASK_FAILED, target + ":" + TASK_ID);
        }
        return new TreeSet<>(baizel.fileSystem().findFiles(outputDir, ".*"));
    }

    private boolean compile(Set<Path> sourceRoots, Set<Path> artifactRoots, Path outputDir, Consumer<Issue> reporter) throws IOException {
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
