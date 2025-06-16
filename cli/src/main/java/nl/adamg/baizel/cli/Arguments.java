package nl.adamg.baizel.cli;

import nl.adamg.baizel.core.model.Target;

import java.util.List;
import java.util.Set;

public final class Arguments {
    public Options options;
    public Set<String> tasks;
    public List<String> taskArgs;
    public Set<Target> targets;

    //region generated code
    public Arguments(Options options, Set<String> tasks, List<String> taskArgs, Set<Target> targets) {
        this.options = options;
        this.tasks = tasks;
        this.taskArgs = taskArgs;
        this.targets = targets;
    }
    //endregion
}
