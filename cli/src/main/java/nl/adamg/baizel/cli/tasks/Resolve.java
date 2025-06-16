package nl.adamg.baizel.cli.tasks;

import nl.adamg.baizel.core.model.Project;
import nl.adamg.baizel.core.model.Target;
import nl.adamg.baizel.core.api.Task;
import nl.adamg.baizel.core.model.TaskInput;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class Resolve implements Task {
    private static final Logger LOG = Logger.getLogger(Resolve.class.getName());
    public static final String TASK_ID = "resolve";

    @Override
    public String getTaskId() {
        return TASK_ID;
    }

    @Override
    public Set<Path> run(Target target, List<String> args, List<TaskInput> inputs, Project project, Baizel baizel) {
        LOG.warning("RESOLVING");
        return Set.of();
    }

    @Override
    public boolean isApplicable(Project project, Target target) {
        return !target.organization().isEmpty() && !target.artifact().isEmpty() && !target.path().isEmpty();
    }
}
