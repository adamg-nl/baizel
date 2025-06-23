package nl.adamg.baizel.core.api;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import nl.adamg.baizel.core.api.TaskScheduler.Input;
import nl.adamg.baizel.internal.common.annotations.ServiceProvider;

@ServiceProvider.Interface
public interface Task {
    /// @return output paths
    Set<Path> run(TargetCoordinates target, List<String> args, List<Input<TaskRequest>> inputs, TargetCoordinates.CoordinateKind targetType, Baizel baizel) throws IOException;

    /// @return pairs of target and task id
    default Set<TaskRequest> findDependencies(TargetCoordinates target, TargetCoordinates.CoordinateKind targetType, Baizel baizel) throws IOException {
        return Set.of();
    }

    default String getTaskId() {
        return getClass().getSimpleName().toLowerCase();
    }

    default boolean isApplicable(TargetCoordinates target, TargetCoordinates.CoordinateKind targetType, Baizel baizel) {
        return true;
    }
}
