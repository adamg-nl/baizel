package nl.adamg.baizel.core.entities;

import java.io.Serializable;
import java.util.List;

/// - API:    [nl.adamg.baizel.core.api.Invocation]
/// - Entity: [nl.adamg.baizel.core.entities.Invocation]
/// - Model:  [nl.adamg.baizel.core.model.Invocation]
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
    //endregion
}
