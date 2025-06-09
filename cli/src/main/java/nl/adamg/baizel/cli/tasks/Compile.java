package nl.adamg.baizel.cli.tasks;

import nl.adamg.baizel.cli.internal.Task;
import nl.adamg.baizel.core.Project;
import nl.adamg.baizel.core.Target;

import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

public class Compile implements Task {
    private static final Logger LOG = Logger.getLogger(Compile.class.getName());
    public static final String TASK_ID = "compile";

    @Override
    public String getTaskId() {
        return TASK_ID;
    }

    @Override
    public List<Request> findDependencies(Project project, Target target, List<String> args) {
        return List.of();
    }

    @Override
    public List<Path> run(Target target, List<String> args, List<Input> inputs, Project project) {
        LOG.warning("COMPILING");
        return List.of();
    }
}
