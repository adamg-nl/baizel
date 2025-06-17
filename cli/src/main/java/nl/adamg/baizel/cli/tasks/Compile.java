package nl.adamg.baizel.cli.tasks;

import com.sun.source.util.JavacTask;
import java.util.Locale;
import javax.tools.Diagnostic;
import nl.adamg.baizel.core.api.BaizelException;
import nl.adamg.baizel.core.api.TaskScheduler;
import nl.adamg.baizel.core.api.Baizel;
import nl.adamg.baizel.core.api.Target;
import nl.adamg.baizel.core.api.Target.Type;
import nl.adamg.baizel.core.api.Task;
import nl.adamg.baizel.core.api.TaskRequest;
import nl.adamg.baizel.core.entities.BaizelErrors;
import nl.adamg.baizel.core.impl.Issue;
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
import nl.adamg.baizel.internal.compiler.Compiler;

public class Compile implements Task {
    private static final Logger LOG = Logger.getLogger(Compile.class.getName());
    public static final String TASK_ID = "compile";
    private static final ThreadLocal<Compiler> COMPILER = ThreadLocal.withInitial(() -> Compiler.create(System.err));

    @ServiceProvider(Task.class)
    public Compile() {
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
    public Set<Path> run(Target target, List<String> args, List<TaskScheduler.Input<TaskRequest>> inputs, Type targetType, Baizel baizel) throws IOException {
        LOG.info("compiling" + LoggerUtil.with("target", target.toString()));
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
        var issues = COMPILER.get().compile(Set.of(sourceRoot), artifactRoots, outputDir);
        for(var issue : issues) {
            if (issue.getKind() == Diagnostic.Kind.ERROR) {
                throw Issue.critical(
                        BaizelErrors.COMPILATION_FAILED,
                        "file", issue.getSource().toUri().getPath(),
                        "lineNumber", issue.getLineNumber() + "",
                        "message", issue.getMessage(Locale.US)
                );
            }
        }
        return new TreeSet<>(baizel.fileSystem().findFiles(outputDir, ".*"));
    }
}
