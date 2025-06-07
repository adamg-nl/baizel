package nl.adamg.baizel.cli.tasks;

import nl.adamg.baizel.cli.Task;
import nl.adamg.baizel.core.entities.Target;
import nl.adamg.baizel.core.entities.Project;

import java.util.List;
import java.util.logging.Logger;

public class Build implements Task {
    private static final Logger LOG = Logger.getLogger(Build.class.getName());

    @Override
    public String getTaskId() {
        return "build";
    }

    @Override
    public void run(Project project, List<Target> targets, List<String> args) {
        LOG.warning(getTaskId() + " task running");
    }
}
