package nl.adamg.baizel.core.entities;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/// - API:    [nl.adamg.baizel.core.api.TaskInput]
/// - Entity: [nl.adamg.baizel.core.entities.TaskInput]
/// - Impl:   [nl.adamg.baizel.core.impl.TaskInputImpl]
public class TaskInput implements Serializable {
    public Target origin;
    public String originTaskId;
    public List<String> paths;

    //region generated code
    public TaskInput(Target origin, String originTaskId, List<String> paths) {
        this.origin = origin;
        this.originTaskId = originTaskId;
        this.paths = paths;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        TaskInput taskInput = (TaskInput) object;
        return Objects.equals(origin, taskInput.origin) && Objects.equals(originTaskId, taskInput.originTaskId) && Objects.equals(paths, taskInput.paths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origin, originTaskId, paths);
    }
    //endregion
}
