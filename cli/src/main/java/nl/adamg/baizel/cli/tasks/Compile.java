package nl.adamg.baizel.cli.tasks;

import java.util.Locale;
import javax.tools.Diagnostic;
import nl.adamg.baizel.core.api.Requirement;
import nl.adamg.baizel.core.api.Baizel;
import nl.adamg.baizel.core.api.TargetCoordinates;
import nl.adamg.baizel.core.api.TargetCoordinates.CoordinateKind;
import nl.adamg.baizel.core.api.Task;
import nl.adamg.baizel.core.api.TaskRequest;
import nl.adamg.baizel.core.api.TaskScheduler.Input;
import nl.adamg.baizel.core.entities.BaizelErrors;
import nl.adamg.baizel.core.impl.Issue;
import nl.adamg.baizel.core.impl.TargetCoordinatesImpl;
import nl.adamg.baizel.core.impl.TaskRequestImpl;
import nl.adamg.baizel.internal.common.annotations.ServiceProvider;
import nl.adamg.baizel.internal.common.util.LoggerUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

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
    public boolean isApplicable(TargetCoordinates target, CoordinateKind targetType, Baizel baizel) {
        return targetType == CoordinateKind.MODULE;
    }

    @Override
    public Set<TaskRequest> findDependencies(TargetCoordinates target, CoordinateKind targetType, Baizel baizel) throws IOException {
        var module = baizel.project().getModule(target);
        var dependencies = new TreeSet<TaskRequest>();
        for (var requirement : module.requirements()) {
            dependencies.addAll(getDependency(requirement, baizel));
        }
        return dependencies;
    }

    private Set<TaskRequest> getDependency(Requirement requirement, Baizel baizel) throws IOException {
        var dependencies = new TreeSet<TaskRequest>();
        if (requirement.isSdkRequirement()) {
            return Set.of();
        }
        var requiredModule = baizel.project().getModuleById(requirement.moduleId());
        if (requiredModule != null) {
            dependencies.add(TaskRequestImpl.of(TargetCoordinatesImpl.module(requiredModule.path()), TASK_ID));
            for(var transitiveRequirement : requiredModule.requirements()) {
                if (! transitiveRequirement.isTransitive()) {
                    continue;
                }
                var transitiveDependency = getDependency(transitiveRequirement, baizel);
                dependencies.addAll(transitiveDependency);
            }
            return dependencies;
        }
        var requiredArtifact = baizel.project().getArtifactCoordinates(requirement.moduleId());
        if (requiredArtifact != null) {
            var artifactTarget = TargetCoordinatesImpl.artifact(requiredArtifact.organization(), requirement.moduleId());
            dependencies.add(TaskRequestImpl.of(artifactTarget, Resolve.TASK_ID));
            return dependencies;
        }
        baizel.report("UNRESOLVED_DEPENDENCY", "moduleId", requirement.moduleId());
        return Set.of();
    }

    @Override
    public Set<Path> run(TargetCoordinates coordinates, List<String> args, List<Input<TaskRequest>> inputs, CoordinateKind coordinateKind, Baizel baizel) throws IOException {
        LOG.info("compiling" + LoggerUtil.with("target", coordinates.toString()));
        var module = baizel.project().getModule(coordinates);
        var targetType = coordinates.targetType();
        var contentRoot = module.getContentRoot(coordinates.targetType());
        if (contentRoot == null) {
            return Set.of(); // nothing to compile
        }
        var artifactRoots = new TreeSet<Path>();
        var outputPathSuffix = Path.of(".build/classes/java/" + targetType.targetId());
        for(var input : inputs) {
            if (input.source().taskId().equals(Resolve.TASK_ID)) {
                artifactRoots.addAll(input.paths());
                continue;
            }
            if (input.source().taskId().equals(Compile.TASK_ID)) {
                for(var path : input.paths()) {
                    var suffixOffset = path.toString().indexOf(outputPathSuffix.toString());
                    if (suffixOffset > 0) {
                        artifactRoots.add(Path.of(path.toString().substring(0, suffixOffset + outputPathSuffix.toString().length())));
                    }
                }
            }
        }
        var outputDir = module.fullPath().resolve(outputPathSuffix);
        Files.createDirectories(outputDir);
        var issues = COMPILER.get().compile(Set.of(contentRoot.fullPath()), artifactRoots, outputDir);
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
