package nl.adamg.baizel.core.api;

/// - API:    [nl.adamg.baizel.core.api.TaskRequest]
/// - Entity: [nl.adamg.baizel.core.entities.TaskRequest]
/// - Model: [nl.adamg.baizel.core.impl.TaskRequestImpl]
public interface TaskRequest extends Comparable<TaskRequest> {
    Target target();
    String taskId();
}
