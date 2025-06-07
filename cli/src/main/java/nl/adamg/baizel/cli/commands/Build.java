package nl.adamg.baizel.cli.commands;

import nl.adamg.baizel.cli.Command;
import nl.adamg.baizel.cli.Target;
import nl.adamg.baizel.core.Project;

import java.util.List;
import java.util.logging.Logger;

public class Build implements Command {
    private static final Logger LOG = Logger.getLogger(Build.class.getName());

    public Build() {
        // constructed by ServiceLoader
    }

    @Override
    public String getName() {
        return "build";
    }

    @Override
    public void run(Project project, List<Target> targets, List<String> args) {
        LOG.warning("build command running");


        // Implementation using BuildGraph and Task classes
        // 1. Create a BuildGraph
        // 2. Add tasks for each target
        // 3. Execute the build

        // Note: In a real implementation, this would use the buildsystem
        // to parse target specifications, create tasks, and build them
    }
}
