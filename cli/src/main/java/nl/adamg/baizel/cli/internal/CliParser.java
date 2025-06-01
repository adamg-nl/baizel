package nl.adamg.baizel.cli.internal;

import nl.adamg.baizel.cli.Baizel;
import nl.adamg.baizel.internal.common.util.collections.Items;

import java.util.*;

public class CliParser {
    /// Format:
    /// ```
    /// baizel [<BAIZEL_OPTION>...] [<COMMAND>...] [<COMMAND_ARG>...] [-- <TARGET>...]
    /// ```
    public static Baizel.Args parse(String... args) {
        var options = new ArrayList<String>();
        var command = new ArrayList<String>();
        var commandArgs = new ArrayList<String>();
        var targets = new ArrayList<String>();

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
        while (! remainingArgs.isEmpty()) {
            var targetPrefixes = Set.of("//", ":", "-:", "-//");
            if (Items.noneMatch(targetPrefixes, p -> remainingArgs.peek().startsWith(p))) {
                commandArgs.add(remainingArgs.poll());
            } else {
                targets.add(remainingArgs.poll());
            }
        }
        return new Baizel.Args(options, command, commandArgs, targets);
    }
}
