package nl.adamg.baizel.core.tasks;

import nl.adamg.baizel.core.Target;
import nl.adamg.baizel.internal.common.util.collections.EntityComparator;

import java.util.Arrays;
import java.util.Objects;

public class TaskRequest implements Comparable<TaskRequest> {
    public Target target;
    public String taskId;

    //region value-like type
    @Override
    public int compareTo(TaskRequest other) {
        return EntityComparator.compareBy(this, other, tr -> tr.target, tr -> tr.taskId);
    }

    @Override
    public String toString() {
        return target + "." + taskId;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[]{target, taskId});
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TaskRequest other &&
                Objects.equals(this.target, other.target) &&
                Objects.equals(this.taskId, other.taskId);
    }

    //endregion

    //region generated code
    public TaskRequest(Target target, String taskId) {
        this.target = target;
        this.taskId = taskId;
    }
    //endregion
}
