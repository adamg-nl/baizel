package nl.adamg.baizel.cli.internal;

import nl.adamg.baizel.cli.Arguments;
import nl.adamg.baizel.cli.Options;
import nl.adamg.baizel.core.Target;
import nl.adamg.baizel.internal.common.serialization.JsonUtil;
import nl.adamg.baizel.internal.common.util.Text;
import nl.adamg.baizel.internal.common.util.collections.Items;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

public class CliParser {
    /// Format:
    /// ```
    /// baizel [<BAIZEL_OPTION>...] [<TASK>...] [<TASK_ARG>...] [-- <TARGET>...]
    /// ```
    public static Arguments parseCliArgs(String... args) {
        if (args.length == 1) {
            var argsString = args[0];
            var base64Decoded = Text.tryDecodeBase64(argsString);
            if (base64Decoded != null && base64Decoded.startsWith("{") && base64Decoded.endsWith("}")) {
                argsString = base64Decoded;
            }
            if (argsString.startsWith("{") && argsString.endsWith("}")) {
                return JsonUtil.fromJson(argsString, Arguments.class);
            }
        }
        var options = new Options();
        var tasks = new TreeSet<String>();
        var taskArgs = new ArrayList<String>();
        var targets = new TreeSet<>(Comparator.comparing(Target::toString));

        if (args.length == 0) {
            return new Arguments(options, tasks, taskArgs, targets);
        }
        var remainingArgs = (Queue<String>)new LinkedList<>(Arrays.asList(args));
        while (! remainingArgs.isEmpty() && remainingArgs.peek().startsWith("-")) {
            parseOption(options, remainingArgs.poll());
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

    private static void parseOption(Options options, String option) {
        var watcherCount = "--watcher-count=";
        if (option.startsWith(watcherCount)) {
            options.workerCount = Integer.parseInt(option.substring(watcherCount.length()));
            return;
        }
        var project = "--project=";
        if (option.startsWith(project)) {
            options.projectRoot = Path.of(option.substring(project.length()));
            return;
        }
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

        return new Target(new nl.adamg.baizel.core.entities.Target(org, mod, path, name));
    }
}
