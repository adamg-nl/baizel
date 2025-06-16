package nl.adamg.baizel.core.entities;

import java.io.Serializable;

/// - API:    [nl.adamg.baizel.core.api.TaskRequest]
/// - Entity: [nl.adamg.baizel.core.entities.TaskRequest]
/// - Model: [nl.adamg.baizel.core.impl.TaskRequestImpl]
public class TaskRequest implements Serializable {
    public Target target;
    public String taskId;

    //region generated code
    public TaskRequest(Target target, String taskId) {
        this.target = target;
        this.taskId = taskId;
    }
    //endregion
}
