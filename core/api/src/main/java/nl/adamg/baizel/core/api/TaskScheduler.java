package nl.adamg.baizel.core.api;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public interface TaskScheduler<Task extends Comparable<Task>> extends AutoCloseable {
    void schedule(Task task, Set<Task> dependencies);

    @Override
    void close() throws IOException, InterruptedException;

    void interrupt();

    /// How to run the task, given the list of inputs?
    @FunctionalInterface
    interface Runner<Task extends Comparable<Task>> {
        Set<Path> run(Task task, List<Input<Task>> inputs) throws IOException;
    }

    record Input<Task extends Comparable<Task>>(Task source, Set<Path> paths) {}
}
