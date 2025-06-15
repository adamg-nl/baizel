package nl.adamg.baizel.core.tasks;

import nl.adamg.baizel.core.Project;
import nl.adamg.baizel.core.Target;

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
    public Set<Path> run(Target target, List<String> args, List<TaskInput> inputs, Project project) {
        LOG.warning("RESOLVING");
        return Set.of();
    }

    @Override
    public boolean isApplicable(Project project, Target target) {
        return !target.organization().isEmpty() && !target.artifact().isEmpty() && !target.path().isEmpty();
    }
}
