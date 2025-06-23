package nl.adamg.baizel.cli.tasks;

import nl.adamg.baizel.core.api.TaskScheduler.Input;
import nl.adamg.baizel.core.api.Baizel;
import nl.adamg.baizel.core.api.TargetCoordinates;
import nl.adamg.baizel.core.api.Task;
import nl.adamg.baizel.core.api.TaskRequest;
import nl.adamg.baizel.internal.common.annotations.ServiceProvider;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public class Clean implements Task {
    public static final String TASK_ID = "clean";

    @ServiceProvider(Task.class)
    public Clean() {
    }

    @Override
    public String getTaskId() {
        return TASK_ID;
    }

    @Override
    public boolean isApplicable(TargetCoordinates target, TargetCoordinates.CoordinateKind targetType, Baizel baizel) {
        return targetType == TargetCoordinates.CoordinateKind.MODULE;
    }

    @Override
    public Set<Path> run(TargetCoordinates target, List<String> args, List<Input<TaskRequest>> inputs, TargetCoordinates.CoordinateKind targetType, Baizel baizel) throws IOException {
        var module = baizel.project().getModule(target);
        baizel.fileSystem().delete(module.fullPath().resolve(".build"));
        return Set.of();
    }
}
