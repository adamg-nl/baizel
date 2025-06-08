package nl.adamg.baizel.cli.tasks;

import nl.adamg.baizel.cli.internal.Task;
import nl.adamg.baizel.core.entities.Project;
import nl.adamg.baizel.core.entities.Target;
import nl.adamg.baizel.internal.jar.JarUtil;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class Jar implements Task {
    private static final Logger LOG = Logger.getLogger(Jar.class.getName());

    @Override
    public String getTaskId() {
        return "jar";
    }

    @Override
    public void run(Project project, List<Target> targets, List<String> args) throws IOException {
        LOG.warning(getTaskId() + " task running");
        JarUtil.update(null, null);
    }
}
