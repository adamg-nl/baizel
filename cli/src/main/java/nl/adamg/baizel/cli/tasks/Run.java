package nl.adamg.baizel.cli.tasks;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import nl.adamg.baizel.core.api.Baizel;
import nl.adamg.baizel.core.api.TargetCoordinates;
import nl.adamg.baizel.core.api.Task;
import nl.adamg.baizel.core.api.TaskRequest;
import nl.adamg.baizel.core.api.TaskScheduler.Input;
import nl.adamg.baizel.internal.common.annotations.ServiceProvider;

public class Run implements Task {
    public static final String TASK_ID = "run";

    @ServiceProvider(Task.class)
    public Run() {}

    @Override
    public Set<Path> run(TargetCoordinates target, List<String> args, List<Input<TaskRequest>> inputs, TargetCoordinates.CoordinateKind targetType, Baizel baizel) throws IOException {
        return Set.of();
    }

    @Override
    public Set<TaskRequest> findDependencies(TargetCoordinates target, TargetCoordinates.CoordinateKind targetType, Baizel baizel) throws IOException {
        return Set.of();
    }

    @Override
    public String getTaskId() {
        return TASK_ID;
    }
}
