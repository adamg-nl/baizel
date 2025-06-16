package nl.adamg.baizel.core.api;

import nl.adamg.baizel.internal.common.annotations.ServiceProvider;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

@ServiceProvider.Interface
public interface Task {
    /// @return output paths
    Set<Path> run(Target target, List<String> args, List<TaskInput> inputs, Baizel baizel);

    /// @return pairs of target and task id
    default Set<TaskRequest> findDependencies(Target target, Baizel baizel) throws IOException {
        return Set.of();
    }

    default String getTaskId() {
        return getClass().getSimpleName().toLowerCase();
    }

    default boolean isApplicable(Target target, Baizel baizel) {
        return true;
    }
}
