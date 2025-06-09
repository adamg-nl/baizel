package nl.adamg.baizel.cli.internal;

import nl.adamg.baizel.core.Project;
import nl.adamg.baizel.core.Target;
import nl.adamg.baizel.internal.common.util.Lazy;
import nl.adamg.baizel.internal.common.util.collections.EntityComparator;

import javax.annotation.CheckForNull;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeMap;

@FunctionalInterface
public interface Task {
    record Request(Target target, String taskId) implements Comparable<Request> {
        @Override
        public int compareTo(Request other) {
            return EntityComparator.compareBy(this, other, Request::target, Request::taskId);
        }
    }

    record Input(Target origin, String originTaskId, List<Path> paths) {}

    @CheckForNull
    static Task get(String taskId) {
        return Cache.TASKS_BY_ID.get().get(taskId);
    }

    static Set<String> getTasks() {
        return Cache.TASKS_BY_ID.get().keySet();
    }

    /**
     * @return output paths
     */
    List<Path> run(Target target, List<String> args, List<Input> inputs, Project project);

    default String getTaskId() {
        return getClass().getSimpleName().toLowerCase();
    }

    /**
     * @return pairs of target and task id
     */
    default List<Request> findDependencies(Project project, Target target, List<String> args) {
        return List.of();
    }

    default boolean isApplicable(Project project, Target target) {
        return true;
    }

    class Cache {
        private static final Lazy.NonNull.Safe<Map<String, Task>> TASKS_BY_ID = Lazy.safeNonNull(() -> {
            var map = new TreeMap<String, Task>();
            for(var task : ServiceLoader.load(Task.class)) {
                map.put(task.getTaskId(), task);
            }
            return map;
        });
    }
}
