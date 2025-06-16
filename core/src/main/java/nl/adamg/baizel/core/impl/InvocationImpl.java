package nl.adamg.baizel.core.impl;

import nl.adamg.baizel.core.api.Invocation;
import nl.adamg.baizel.core.api.Target;
import nl.adamg.baizel.internal.common.util.EntityModel;
import nl.adamg.baizel.internal.common.util.collections.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

/// Format:
/// ```
/// <TASK>...] [<TASK_ARG>...] [-- <TARGET>...]
/// ```
///
/// - API:    [nl.adamg.baizel.core.api.Invocation]
/// - Entity: [nl.adamg.baizel.core.entities.Invocation]
/// - Model:  [nl.adamg.baizel.core.impl.InvocationImpl]
public class InvocationImpl
        extends EntityModel<Invocation, nl.adamg.baizel.core.entities.Invocation, InvocationImpl>
        implements Invocation {
    //region factory
    public Invocation of(
            List<String> tasks,
            List<String> taskArgs,
            List<nl.adamg.baizel.core.entities.Target> targets
    ) {
        return new InvocationImpl(
                new nl.adamg.baizel.core.entities.Invocation(
                        tasks,
                        taskArgs,
                        targets
                )
        );
    }

    public static nl.adamg.baizel.core.entities.Invocation parse(Queue<String> remainingArgs) {
        var tasks = new ArrayList<String>();
        var taskArgs = new ArrayList<String>();
        var targets = new ArrayList<nl.adamg.baizel.core.entities.Target>();
        var invocation = new nl.adamg.baizel.core.entities.Invocation(tasks, taskArgs, targets);

        var targetPrefixes = Set.of("//", ":", "-:", "-//");
        while (! remainingArgs.isEmpty() &&
                ! remainingArgs.peek().startsWith("-") &&
                Items.noneMatch(targetPrefixes, remainingArgs.peek()::startsWith)) {
            tasks.add(remainingArgs.poll());
        }
        var hasTaskArgs = false;
        while (! remainingArgs.isEmpty()) {
            var nextArg = remainingArgs.peek();
            var isOptionLikeTarget = nextArg.startsWith("-:") || nextArg.startsWith("-//");
            boolean isOption = nextArg.startsWith("-") && ! isOptionLikeTarget;
            if ((hasTaskArgs || isOption) && ! nextArg.equals("--")) {
                hasTaskArgs = true;
                taskArgs.add(remainingArgs.poll());
            } else {
                break;
            }
        }
        if ("--".equals(remainingArgs.peek())) {
            remainingArgs.remove();
        }
        while (! remainingArgs.isEmpty()) {
            var next = remainingArgs.poll();
            if (! remainingArgs.isEmpty() && Items.noneMatch(targetPrefixes, next::startsWith)) {
                taskArgs.add(next);
            } else {
                targets.add(TargetImpl.parseTarget(next));
            }
        }
        return invocation;
    }
    //endregion

    //region getters
    @Override
    public Set<String> tasks() {
        return new TreeSet<>(entity.tasks);
    }

    @Override
    public List<String> taskArgs() {
        return entity.taskArgs;
    }

    @Override
    public Set<Target> targets() {
        return Items.mapToSet(entity.targets, TargetImpl::new);
    }
    //endregion

    //region entity model
    @Override
    public String toString() {
        return String.join(" ", entity.tasks) + " " +
                String.join(" ", entity.taskArgs) + " " +
                Items.toString(targets(), " ", Object::toString);
    }

    @Override
    protected List<Function<nl.adamg.baizel.core.entities.Invocation, ?>> fields() {
        return List.of(
                e -> e.targets,
                e -> e.taskArgs,
                e -> e.tasks
        );
    }
    //endregion

    //region generated code
    public InvocationImpl(nl.adamg.baizel.core.entities.Invocation entity) {
        super(entity);
    }
    //endregion
}
