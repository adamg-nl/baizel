package nl.adamg.baizel.core.api;

/// - API:    [nl.adamg.baizel.core.api.TaskRequest]
/// - Entity: [nl.adamg.baizel.core.entities.TaskRequest]
/// - Impl:   [nl.adamg.baizel.core.impl.TaskRequestImpl]
@SuppressWarnings("JavadocReference")
public interface TaskRequest extends Comparable<TaskRequest> {
    TargetCoordinates target();
    String taskId();
    @Override String toString();
    @Override boolean equals(Object other);
    @Override int hashCode();
}
