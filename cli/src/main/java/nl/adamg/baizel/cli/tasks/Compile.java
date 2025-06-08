package nl.adamg.baizel.cli.tasks;

import nl.adamg.baizel.cli.internal.Task;
import nl.adamg.baizel.core.entities.Project;
import nl.adamg.baizel.core.entities.Target;

import java.time.Instant;
import java.util.List;
import java.util.logging.Logger;

public class Compile implements Task {
    private static final Logger LOG = Logger.getLogger(Compile.class.getName());

    @Override
    public String getTaskId() {
        return "compile";
    }

    @Override
    public void run(Project project, List<Target> targets, List<String> args) {
        System.out.println(getTaskId() + " task running " + Instant.now());
    }
}
