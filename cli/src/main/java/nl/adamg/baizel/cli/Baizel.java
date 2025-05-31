package nl.adamg.baizel.cli;

import nl.adamg.baizel.cli.internal.CliParser;
import nl.adamg.baizel.internal.common.util.collections.Collections;

import javax.annotation.CheckForNull;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

/// # Baizel build system
///
/// - Usage: `baizel [<BAIZEL_OPTION>...] <COMMAND> [<COMMAND_ARG>...] [-- <TARGET>...]`
/// - Usage: `baizel <CLASS_TARGET> <ARGS>...`
///
/// ## Arguments:
/// - `TARGET` can use one of the formats:
///   -       `//path/to/package:target_name`  a specific target within a package
///   -       `//path/to/package`              the main target within a package
///   -       `//path/to/package/...`          all targets within a package and its subpackages
///   -       `//path/to/package:*`            all targets directly within a package
///   -       `:target_name`                   a shorthand for a target in the current package
///   -       `-//path/to/package:target_name` excludes a specific target
///
/// ## Commands:
/// - `build` Builds the specified targets.
/// - `test`  Builds and runs the specified test targets.
/// - `run`   Builds and runs a single executable target.
/// - `clean` Removes Baizel's output files and invalidates local cache.
/// - `help`  Provides help for commands or the index.
public class Baizel {
    private static final Logger LOG = Logger.getLogger(Baizel.class.getName());

    public record Args(List<String> options, String command, List<String> commandArgs, List<String> targets) {}

    public static void main(String... args) {
        LOG.info("Hello Baizel");
//        main(CliParser.parse(args));
    }

    public static void main(Args args) {

    }

    @CheckForNull
    private static Path inferProjectRoot(Set<String> requestedModulePaths, Path currentDir) {
        var level = currentDir;
        while (level != null) {
            var allDepsMatch = Collections.allMatch(requestedModulePaths, p -> Files.isDirectory(currentDir.resolve(p)));
            if (allDepsMatch) {
                return level;
            }
            level = level.getParent();
        }
        return null;
    }

    private record TaskRequest(String module, String taskName) {}

    private static List<TaskRequest> parseEntryTasks(String... args) {
        var tasks = new TreeMap<String, TaskRequest>();
        for(var arg : args) {
            if (arg.startsWith("-")) {
                return new ArrayList<>(tasks.values());
            }
            var module = arg.contains(":") ? arg.replaceAll(":.*", "") : "";
            var taskName = arg.contains(":") ? arg.replaceAll(".*:", "") : arg;
            tasks.put(arg, new TaskRequest(module, taskName));
        }
        return new ArrayList<>(tasks.values());
    }
}
