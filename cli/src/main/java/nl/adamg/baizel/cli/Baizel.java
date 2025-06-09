package nl.adamg.baizel.cli;

import nl.adamg.baizel.cli.internal.CliParser;
import nl.adamg.baizel.cli.internal.Task;
import nl.adamg.baizel.core.entities.Target;
import nl.adamg.baizel.internal.common.java.Services;
import nl.adamg.baizel.internal.common.util.collections.Items;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class Baizel {
    private static final Logger LOG = Logger.getLogger(Baizel.class.getName());
    public record Args(Set<String> options, Set<String> tasks, Set<String> taskArgs, Set<Target> targets) {}
    public static String HELP = """
            Baizel build system for Java™
           
            Usage:
              baizel [<BAIZEL_OPTION>...] <TASK>... [<TASK_ARG>...] [-- <TARGET>...]
              baizel <SOURCE_FILE_TARGET> <ENTRY_POINT_ARGS>...
              baizel ( <JSON_ARGS_OBJECT> | <BASE_64_JSON_ARGS_OBJECT> )
            
            Usage examples:
              baizel --verbose build --no-cache //internal/common/util
              baizel //src/main/java/Hello.java "world" "42"
              baizel '{ "tasks": [ "build" ], "targets": [ "//bootstrap" ] }'
           
            To get detailed information about a particular task, use 'baizel <TASK> --help'.
            
            If the arguments are provided as JSON or Base64-encoded JSON, then output (stdout) and logging (stderr)
            will be in format of newline-separated JSON object(s), and the command may accept input event objects
            via stdin in the same format.
           
            Tasks:
              build            builds the specified targets
              test             runs the specified test targets
              run              runs the specified executable targets
              clean            removes Baizel's output files and invalidates local cache
              shell            opens jshell session with given target's classpath
           
            Target syntax:
              <TARGET>         is in format [-][@[<ORG>/]<MODULE>][//<PATH>][:<TARGET_NAME>] 
                               means either a source set name (as in "<MODULE>/src/<SOURCE_SET_NAME>/java"),
                               or a custom target defined by an extension
              <PATH>           can end with ... to match all targets within a package and its subpackages
              <PATH>           can contain ** to match any number of intermediate directories and files
              <TARGET_NAME>    equal to * means all the targets directly within package
              <TARGET_NAME>    missing means the "main" target
              <PATH>           missing means the current package
              <ORG>/<MODULE>   when defined mean running a task from remote package
              -<TARGET>        means excluding that target
            
            Target syntax examples:
              //pkg:target     a specific target within a package
              //pa/cka/ge      the main target within a package
              //pa/cka/ge/...  all targets within a package and its subpackages
              //pa/cka/ge:*    targets directly within a package
              :target          a shorthand for a target in the current package
              -//pkg:target    excludes a specific target within a package
           
            Environment variables used:
              BAIZEL_DEBUG     enables JVM waiting debugger. Values: true, false, or port number (default port: 5005)
              BAIZEL_VERBOSE   enables verbose logging. Values: true, false
              BAIZEL_JVM_OPTS  additional JVM arguments, space-separated
            """;

    public static void main(String... args) throws Exception {
        LOG.info("main(" + String.join(", ", args) + ")");
        main(CliParser.parseCliArgs(args));
    }

    public static void main(Args args) throws Exception {
        if (args.tasks().isEmpty()) {
            throw CliErrors.TASK_NOT_SELECTED.exit();
        }
        var allTasks = Services.get(Task.class);
        var missingTasks = Items.filter(args.tasks, c -> Items.noneMatch(allTasks, ec -> ec.getTaskId().equals(c)));
        if (! missingTasks.isEmpty()) {
            var taskNames = Items.mapToList(allTasks, Task::getTaskId);
            throw CliErrors.UNKNOWN_TASK.exit(String.join(", ", missingTasks), String.join(", ", taskNames));
        }
        var matchingTasks = Items.filter(allTasks, c -> args.tasks.contains(c.getTaskId()));
        for(var task : matchingTasks) {
            task.run(null, List.of(), List.of());
        }
    }
}
