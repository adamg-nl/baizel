package nl.adamg.baizel.cli.tasks;

import nl.adamg.baizel.core.entities.Issue;
import nl.adamg.baizel.core.tasks.Task;
import nl.adamg.baizel.core.Project;
import nl.adamg.baizel.core.Target;
import nl.adamg.baizel.core.tasks.TaskInput;
import nl.adamg.baizel.core.tasks.TaskRequest;
import nl.adamg.baizel.internal.common.util.LoggerUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

public class Compile implements Task {
    private static final Logger LOG = Logger.getLogger(Compile.class.getName());
    public static final String TASK_ID = "compile";

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
    public Set<Path> run(Target target, List<String> args, List<TaskInput> inputs, Project project) {
        LOG.warning("compiling" + LoggerUtil.with("target", target.toString()));
        return Set.of();
    }
}
