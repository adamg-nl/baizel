package nl.adamg.baizel.cli.tasks;

import nl.adamg.baizel.core.api.Baizel;
import nl.adamg.baizel.core.api.Target;
import nl.adamg.baizel.core.api.Task;
import nl.adamg.baizel.core.api.TaskInput;
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
    public Set<Path> run(Target target, List<String> args, List<TaskInput> inputs, Baizel baizel) throws IOException {
        var module = target.getModule(baizel.project());
        if (module == null) {
            baizel.report("MODULE_NOT_FOUND", "path", target.path());
        } else {
            baizel.fileSystem().delete(module.fullPath().resolve(".build"));
        }
        return Set.of();
    }
}
