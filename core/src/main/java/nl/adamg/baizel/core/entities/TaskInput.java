package nl.adamg.baizel.core.entities;

import java.io.Serializable;
import java.util.List;

/// - API:    [nl.adamg.baizel.core.api.TaskInput]
/// - Entity: [nl.adamg.baizel.core.entities.TaskInput]
/// - Model:  [nl.adamg.baizel.core.model.TaskInput]
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
    //endregion
}
