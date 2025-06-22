package nl.adamg.baizel.cli.tasks;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import nl.adamg.baizel.core.api.Baizel;
import nl.adamg.baizel.core.api.Target;
import nl.adamg.baizel.core.api.Task;
import nl.adamg.baizel.core.api.TaskRequest;
import nl.adamg.baizel.core.api.TaskScheduler.Input;
import nl.adamg.baizel.core.impl.TaskRequestImpl;
import nl.adamg.baizel.internal.common.annotations.ServiceProvider;

public class Build implements Task {
    public static final String TASK_ID = "build";

    @ServiceProvider(Task.class)
    public Build() {}

    @Override
    public Set<Path> run(Target target, List<String> args, List<Input<TaskRequest>> inputs, Target.Type targetType, Baizel baizel) throws IOException {
        return Set.of();
    }

    @Override
    public Set<TaskRequest> findDependencies(Target target, Target.Type targetType, Baizel baizel) {
        return Set.of(
                TaskRequestImpl.of(target, "jar")
        );
    }

    @Override
    public String getTaskId() {
        return TASK_ID;
    }
}
