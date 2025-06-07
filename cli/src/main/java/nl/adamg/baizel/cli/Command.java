package nl.adamg.baizel.cli;

import nl.adamg.baizel.core.Project;

import java.util.List;

public interface Command {
    void run(Project project, List<Target> targets, List<String> args);
    String getName();
}
