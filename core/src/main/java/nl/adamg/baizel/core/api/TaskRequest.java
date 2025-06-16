package nl.adamg.baizel.core.api;

import nl.adamg.baizel.core.model.Target;

/// - API:    [nl.adamg.baizel.core.api.TaskRequest]
/// - Entity: [nl.adamg.baizel.core.entities.TaskRequest]
/// - Model:  [nl.adamg.baizel.core.model.TaskRequest]
public interface TaskRequest {
    Target target();
    String taskId();
}
