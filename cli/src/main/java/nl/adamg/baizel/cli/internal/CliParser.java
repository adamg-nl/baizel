package nl.adamg.baizel.cli.internal;

import nl.adamg.baizel.cli.Arguments;
import nl.adamg.baizel.core.entities.Target;
import nl.adamg.baizel.internal.common.util.collections.Items;

import java.util.*;

public class CliParser {
    /// Format:
    /// ```
    /// baizel [<BAIZEL_OPTION>...] [<TASK>...] [<TASK_ARG>...] [-- <TARGET>...]
    /// ```
    public static Arguments parseCliArgs(String... args) {
        var options = new TreeSet<String>();
        var tasks = new TreeSet<String>();
        var taskArgs = new TreeSet<String>();
        var targets = new TreeSet<>(Comparator.comparing(Target::toString));

        if (args.length == 0) {
            return new Arguments(options, tasks, taskArgs, targets);
        }
        var remainingArgs = (Queue<String>)new LinkedList<>(Arrays.asList(args));
        while (! remainingArgs.isEmpty() && remainingArgs.peek().startsWith("-")) {
            options.add(remainingArgs.poll());
        }
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
                targets.add(parseTarget(next));
            }
        }
        return new Arguments(options, tasks, taskArgs, targets);
    }

    public static Target parseTarget(String input) {
        var org = (String)null;
        var mod = (String)null;
        var name = (String)null;

        var at = input.indexOf('@');
        var slashSlash = input.indexOf("//");
        var colon = input.indexOf(':');

        if (at != -1 && at < slashSlash) {
            var orgMod = input.substring(at + 1, slashSlash).split("/", 2);
            org = orgMod[0];
            if (orgMod.length > 1) {
                mod = orgMod[1];
            }
        }

        var pathStart = slashSlash + 2;
        var pathEnd = colon != -1 ? colon : input.length();
        var path = input.substring(pathStart, pathEnd);

        if (colon != -1) {
            name = input.substring(colon + 1);
        }

        return new Target(org, mod, path, name);
    }
}
