package nl.adamg.baizel.core.entities;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/// - API:    [nl.adamg.baizel.core.api.Invocation]
/// - Entity: [nl.adamg.baizel.core.entities.Invocation]
/// - Impl:   [nl.adamg.baizel.core.impl.InvocationImpl]
@SuppressWarnings("JavadocReference")
public class Invocation implements Serializable {
    public List<String> tasks;
    public List<String> taskArgs;
    public List<TargetCoordinates> targets;

    //region generated code
    public Invocation(
            List<String> tasks,
            List<String> taskArgs,
            List<TargetCoordinates> targets
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
