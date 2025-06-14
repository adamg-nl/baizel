package nl.adamg.baizel.cli.tasks;

import nl.adamg.baizel.core.tasks.Task;
import nl.adamg.baizel.core.Project;
import nl.adamg.baizel.core.Target;
import nl.adamg.baizel.core.tasks.TaskInput;
import nl.adamg.baizel.core.tasks.TaskRequest;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class Compile implements Task {
    private static final Logger LOG = Logger.getLogger(Compile.class.getName());
    public static final String TASK_ID = "compile";

    @Override
    public String getTaskId() {
        return TASK_ID;
    }

    @Override
    public Set<TaskRequest> findDependencies(Project project, Target target, List<String> args) {
        return Set.of();
    }

    @Override
    public Set<Path> run(Target target, List<String> args, List<TaskInput> inputs, Project project) {
        LOG.warning("COMPILING");
        return Set.of();
    }
}
