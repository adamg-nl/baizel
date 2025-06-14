package nl.adamg.baizel.core.tasks;

import nl.adamg.baizel.internal.common.util.Lazy;

import javax.annotation.CheckForNull;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeMap;

public enum Tasks {
    ;

    private static final Lazy.NonNull.Safe<Map<String, Task>> TASKS_BY_ID = Lazy.safeNonNull(() -> {
        var map = new TreeMap<String, Task>();
        for(var task : ServiceLoader.load(Task.class)) {
            map.put(task.getTaskId(), task);
        }
        return map;
    });

    public static Task get(String taskId) {
        var task = TASKS_BY_ID.get().get(taskId);
        if (task == null) {
            throw new IllegalArgumentException("task not found: " + taskId);
        }
        return task;
    }

    public static Set<String> getTasks() {
        return TASKS_BY_ID.get().keySet();
    }
}
