package nl.adamg.baizel.core.entities;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/// - API:    [nl.adamg.baizel.core.api.Invocation]
/// - Entity: [nl.adamg.baizel.core.entities.Invocation]
/// - Impl:   [nl.adamg.baizel.core.impl.InvocationImpl]
public class Invocation implements Serializable {
    public List<String> tasks;
    public List<String> taskArgs;
    public List<Target> targets;

    //region generated code
    public Invocation(
            List<String> tasks,
            List<String> taskArgs,
            List<Target> targets
    ) {
        this.tasks = tasks;
        this.taskArgs = taskArgs;
        this.targets = targets;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        Invocation that = (Invocation) object;
        return Objects.equals(tasks, that.tasks) && Objects.equals(taskArgs, that.taskArgs) && Objects.equals(targets, that.targets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tasks, taskArgs, targets);
    }
    //endregion
}
