package nl.adamg.baizel.core.tasks;

import nl.adamg.baizel.core.Project;
import nl.adamg.baizel.core.Target;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

@FunctionalInterface
public interface Task {
    /**
     * @return output paths
     */
    Set<Path> run(Target target, List<String> args, List<TaskInput> inputs, Project project);

    default String getTaskId() {
        return getClass().getSimpleName().toLowerCase();
    }

    /**
     * @return pairs of target and task id
     */
    default Set<TaskRequest> findDependencies(Project project, Target target) throws IOException {
        return Set.of();
    }

    default boolean isApplicable(Project project, Target target) {
        return true;
    }
}
