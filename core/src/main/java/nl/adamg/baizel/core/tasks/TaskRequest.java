package nl.adamg.baizel.core.tasks;

import nl.adamg.baizel.core.Target;
import nl.adamg.baizel.internal.common.util.EntityModel;
import nl.adamg.baizel.internal.common.util.collections.EntityComparator;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class TaskRequest extends EntityModel<TaskRequest, TaskRequest> {
    public Target target;
    public String taskId;

    //region entity model
    @Override
    public String toString() {
        return target + ":" + taskId;
    }

    @Override
    protected List<Function<TaskRequest, ?>> fields() {
        return List.of(
                tr -> tr.target,
                tr -> tr.taskId
        );
    }
    //endregion

    //region generated code
    public TaskRequest(Target target, String taskId) {
        this.target = target;
        this.taskId = taskId;
    }
    //endregion
}
