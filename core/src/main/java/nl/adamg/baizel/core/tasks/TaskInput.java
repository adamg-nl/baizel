package nl.adamg.baizel.core.tasks;

import nl.adamg.baizel.core.Target;
import nl.adamg.baizel.internal.common.util.EntityModel;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public class TaskInput extends EntityModel<TaskInput, TaskInput> {
    public Target origin;
    public String originTaskId;
    public Set<Path> paths;

    //region entity model
    @Override
    public String toString() {
        return origin + "." + originTaskId;
    }

    @Override
    protected List<Function<TaskInput, ?>> fields() {
        return List.of(
                ti -> ti.origin,
                ti -> ti.originTaskId,
                ti -> ti.paths
        );
    }
    //endregion

    //region generated code
    public TaskInput(Target origin, String originTaskId, Set<Path> paths) {
        this.origin = origin;
        this.originTaskId = originTaskId;
        this.paths = paths;
    }
    //endregion
}
