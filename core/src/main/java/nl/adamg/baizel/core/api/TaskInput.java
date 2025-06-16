package nl.adamg.baizel.core.api;

import java.nio.file.Path;
import java.util.Set;

/// - API:    [nl.adamg.baizel.core.api.TaskInput]
/// - Entity: [nl.adamg.baizel.core.entities.TaskInput]
/// - Model:  [nl.adamg.baizel.core.model.TaskInput]
public interface TaskInput {
    Target origin();
    String originTaskId();
    Set<Path> paths();
}
