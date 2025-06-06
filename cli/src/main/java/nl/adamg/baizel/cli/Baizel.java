package nl.adamg.baizel.cli;

import nl.adamg.baizel.cli.internal.CliParser;
import nl.adamg.baizel.internal.common.util.collections.Items;

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
/// To get detailed information about a particular command, use `baizel <COMMAND> --help`.
///
/// ## Targets:
/// - `<TARGET>` is in format `[@[<ORG>/][<MODULE>]]//<PATH>[:<TARGET_NAME>]`
/// - `<PATH>` can end with `...` to match all targets within a package and its subpackages.
/// - `<PATH>` can contain `**` to match any number of intermediate directories and files.
/// - `<TARGET_NAME>` equal to `*` means all the targets directly within package.
/// - `<TARGET_NAME>` missing means the main target.
/// - `<PATH>` missing means the current package.
/// - `<ORG>/<MODULE>` when defined mean running a task from remote package.
/// - `-<TARGET>` means excluding that target
///
/// ## Commands:
/// - `build` Builds the specified targets.
/// - `test`  Runs the sspecified test targets.
/// - `run`   Runs the specified executable targets.
/// - `clean` Removes Baizel's output files and invalidates local cache.
/// - `shell` Opens `jshell` session with given target's classpath
///
/// ## Environment variables used:
/// - `BAIZEL_DEBUG`    Enables JVM waiting debugger. Values: true, false, or port number (default port: 5005)
/// - `BAIZEL_VERBOSE`  Enables verbose logging. Values: true, false
/// - `BAIZEL_JVM_OPTS` Additional JVM arguments, space-separated
public class Baizel {
    private static final Logger LOG = Logger.getLogger(Baizel.class.getName());

    public record Args(List<String> options, List<String> commands, List<String> commandArgs, List<Target> targets) {}

    public static void main(String... args) {
        LOG.info("Hello Baizel");
        main(CliParser.parse(args));
    }

    public static void main(Args args) {

    }
}
