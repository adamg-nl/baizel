package nl.adamg.baizel.core.tasks;

import nl.adamg.baizel.core.Target;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class TaskInput {
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
