package nl.adamg.baizel.cli;

import nl.adamg.baizel.cli.internal.CliParser;
import nl.adamg.baizel.core.entities.Target;
import nl.adamg.baizel.internal.common.java.Services;
import nl.adamg.baizel.internal.common.util.collections.Items;

import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Logger;

public class Baizel {
    private static final Logger LOG = Logger.getLogger(Baizel.class.getName());
    public record Args(Set<String> options, Set<String> commands, Set<String> commandArgs, Set<Target> targets) {}
    public static String HELP = """
            Baizel build system
           
            Usage: baizel [<BAIZEL_OPTION>...] <COMMAND>... [<COMMAND_ARG>...] [-- <TARGET>...]
            Usage: baizel <CLASS_TARGET> <ARGS>...
            Usage: baizel ( <JSON_ARGS_OBJECT> | <BASE_64_JSON_ARGS_OBJECT> )
            
            Example: baizel --verbose build --no-cache //internal/common/util
            Example: baizel //src/main/java/Hello.java "world" "42"
            Example: baizel '{ "commands": [ "build" ], "targets": [ "//bootstrap" ] }'
           
            To get detailed information about a particular command, use baizel <COMMAND> --help.
            
            If the arguments are provided as JSON or Base64-encoded JSON, then output (stdout) and logging (stderr)
            will be in format of newline-separated JSON object(s), and the command may accept input event objects
            via stdin in the same format.
           
            Target syntax:
              <TARGET>         is in format [@[<ORG>/]<MODULE>][//<PATH>][:<TARGET_NAME>] .
              <PATH>           can end with ... to match all targets within a package and its subpackages.
              <PATH>           can contain ** to match any number of intermediate directories and files.
              <TARGET_NAME>    equal to * means all the targets directly within package.
              <TARGET_NAME>    missing means the "main" target.
              <PATH>           missing means the current package.
              <ORG>/<MODULE>   when defined mean running a task from remote package.
              -<TARGET>        means excluding that target.
           
            Commands:
              build            Builds the specified targets.
              test             Runs the specified test targets.
              run              Runs the specified executable targets.
              clean            Removes Baizel's output files and invalidates local cache.
              shell            Opens jshell session with given target's classpath.
           
            Environment variables used:
              BAIZEL_DEBUG     Enables JVM waiting debugger. Values: true, false, or port number (default port: 5005)
              BAIZEL_VERBOSE   Enables verbose logging. Values: true, false
              BAIZEL_JVM_OPTS  Additional JVM arguments, space-separated
            """;

    public static void main(String... args) {
        LOG.info("Hello Baizel");
        main(CliParser.parse(args));
    }

    public static void main(Args args) {
        if (args.commands().isEmpty()) {
            throw CliErrors.COMMAND_NOT_SELECTED.exit();
        }
        var allCommands = Services.get(Command.class);
        var missingCommands = Items.filter(args.commands, c -> Items.noneMatch(allCommands, ec -> ec.getName().equals(c)));
        if (! missingCommands.isEmpty()) {
            throw CliErrors.UNKNOWN_COMMAND.exit(String.join(", ", missingCommands));
        }
        var matchingCommands = Items.filter(allCommands, c -> args.commands.contains(c.getName()));
        for(var command : matchingCommands) {

        }

    }
}
