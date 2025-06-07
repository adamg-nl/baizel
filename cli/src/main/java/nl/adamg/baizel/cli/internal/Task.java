package nl.adamg.baizel.cli.internal;

import nl.adamg.baizel.core.entities.Project;
import nl.adamg.baizel.core.entities.Target;

import java.util.List;

public interface Task {
    void run(Project project, List<Target> targets, List<String> args);
    String getTaskId();
}
