package nl.adamg.baizel.core.entities;

import java.io.Serializable;
import java.util.Objects;
import nl.adamg.baizel.internal.common.annotations.Entity;

/// - API:    [nl.adamg.baizel.core.api.TaskRequest]
/// - Entity: [nl.adamg.baizel.core.entities.TaskRequest]
/// - Impl:   [nl.adamg.baizel.core.impl.TaskRequestImpl]
@SuppressWarnings("JavadocReference")
@Entity
public class TaskRequest implements Serializable {
    public Target target;
    public String taskId;

    //region generated code
    public TaskRequest(Target target, String taskId) {
        this.target = target;
        this.taskId = taskId;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        TaskRequest that = (TaskRequest) object;
        return Objects.equals(target, that.target) && Objects.equals(taskId, that.taskId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, taskId);
    }
    //endregion
}
