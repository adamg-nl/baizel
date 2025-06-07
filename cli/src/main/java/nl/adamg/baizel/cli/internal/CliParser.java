package nl.adamg.baizel.cli.internal;

import nl.adamg.baizel.cli.Baizel;
import nl.adamg.baizel.core.entities.Target;
import nl.adamg.baizel.internal.common.util.collections.Items;

import java.util.*;

public class CliParser {
    /// Format:
    /// ```
    /// baizel [<BAIZEL_OPTION>...] [<COMMAND>...] [<COMMAND_ARG>...] [-- <TARGET>...]
    /// ```
    public static Baizel.Args parse(String... args) {
        var options = new TreeSet<String>();
        var command = new TreeSet<String>();
        var commandArgs = new TreeSet<String>();
        var targets = new TreeSet<>(Comparator.comparing(Target::toString));

        if (args.length == 0) {
            return new Baizel.Args(options, command, commandArgs, targets);
        }
        var remainingArgs = (Queue<String>)new LinkedList<>(Arrays.asList(args));
        while (! remainingArgs.isEmpty() && remainingArgs.peek().startsWith("-")) {
            options.add(remainingArgs.poll());
        }
        while (! remainingArgs.isEmpty() && ! remainingArgs.peek().startsWith("-")) {
            command.add(remainingArgs.poll());
        }
        var hasCommandArgs = false;
        while (! remainingArgs.isEmpty()) {
            var nextArg = remainingArgs.peek();
            var isOptionLikeTarget = nextArg.startsWith("-:") || nextArg.startsWith("-//");
            boolean isOption = nextArg.startsWith("-") && ! isOptionLikeTarget;
            if ((hasCommandArgs || isOption) && ! nextArg.equals("--")) {
                hasCommandArgs = true;
                commandArgs.add(remainingArgs.poll());
            } else {
                break;
            }
        }
        if ("--".equals(remainingArgs.peek())) {
            remainingArgs.remove();
        }
        while (! remainingArgs.isEmpty()) {
            var next = remainingArgs.poll();
            var targetPrefixes = Set.of("//", ":", "-:", "-//");
            if (! remainingArgs.isEmpty() && Items.noneMatch(targetPrefixes, p -> remainingArgs.peek().startsWith(p))) {
                commandArgs.add(next);
            } else {
                targets.add(parseTarget(next));
            }
        }
        return new Baizel.Args(options, command, commandArgs, targets);
    }

    public static Target parseTarget(String input) {
        var org = (String)null;
        var mod = (String)null;
        String path;
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
        path = input.substring(pathStart, pathEnd);

        if (colon != -1) {
            name = input.substring(colon + 1);
        } else if (!path.isEmpty()) {
            name = path.substring(path.lastIndexOf('/') + 1);
        }

        return new Target(org, mod, path, name);
    }
}
