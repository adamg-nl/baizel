package nl.adamg.baizel.core.entities;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.Set;

/// - API:    [nl.adamg.baizel.core.api.TaskInput]
/// - Entity: [nl.adamg.baizel.core.entities.TaskInput]
/// - Model:  [nl.adamg.baizel.core.model.TaskInput]
public class TaskInput implements Serializable {
    public Target origin;
    public String originTaskId;
    public Set<Path> paths;

    //region generated code
    public TaskInput(Target origin, String originTaskId, Set<Path> paths) {
        this.origin = origin;
        this.originTaskId = originTaskId;
        this.paths = paths;
    }
    //endregion
}
