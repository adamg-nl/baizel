package nl.adamg.baizel.cli;

import nl.adamg.baizel.cli.internal.CliParser;
import nl.adamg.baizel.core.TaskScheduler;
import nl.adamg.baizel.core.Project;
import nl.adamg.baizel.core.Target;
import nl.adamg.baizel.core.entities.Issue;
import nl.adamg.baizel.core.tasks.Task;
import nl.adamg.baizel.core.tasks.TaskRequest;
import nl.adamg.baizel.core.tasks.Tasks;
import nl.adamg.baizel.internal.common.util.LoggerUtil;
import nl.adamg.baizel.internal.common.util.collections.Items;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class Baizel {
    private static final Logger LOG = Logger.getLogger(Baizel.class.getName());
    private static final String HELP = """
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
              <TARGET>         is in format [-][@[<ORG>/]<ARTIFACT>][//<PATH>][:<TARGET_NAME>]
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
           
            Baizel options:
              --worker-count=<N>  (optional, default: number of CPU cores)
              --project=<PATH>    (optional, default: innermost project containing $PWD, or $PWD if none found)
              --debug             (optional) listen for debugger on port 5005 and enable verbose logging
           
            Environment variables used:
              BAIZEL_DEBUG     enables JVM waiting debugger. Values: true, false, or port number (default port: 5005)
              BAIZEL_VERBOSE   enables verbose logging. Values: true, false
              BAIZEL_JVM_OPTS  additional JVM arguments, space-separated
            """;
    /// When Baizel instance is reused to run the same or overlapping set of tasks later,
    /// thanks to this map their task dependencies will not need to be recomputed.
    private final Map<TaskRequest, Set<TaskRequest>> taskDependencyCache = new ConcurrentHashMap<>();
    private final Project project;
    private final Consumer<Issue> reporter;

    /// CLI entry point to the Baizel build system for Java™
    public static void main(String... rawArgs) throws Exception {
        LOG.info("main() started" + LoggerUtil.with("rawArgs", String.join(", ", rawArgs)));
        if (rawArgs.length == 1 && "--help".equals(rawArgs[0])) {
            System.err.println(HELP);
            return;
        }
        var args = CliParser.parseCliArgs(rawArgs);
        var reporter = (Consumer<Issue>) i -> LOG.warning(i.id + LoggerUtil.with(i.details));
        new Baizel(Project.load(args.options.projectRoot, reporter), reporter).run(args);
        LOG.info("main() finished");
    }

    /// Run specified tasks and their transitive dependencies on the Baizel worker pool
    public void run(Arguments arguments) throws Exception {
        if (arguments.tasks.isEmpty()) {
            throw CliErrors.TASK_NOT_SELECTED.exit();
        }
        var taskDependencies = collectTaskDependencies(arguments.tasks, arguments.targets);
        TaskScheduler.scheduleAndWait(taskDependencies, arguments.options.workerCount, getRunner(arguments));
    }

    private TaskScheduler.Runner getRunner(Arguments arguments) {
        return (task, inputs) -> Tasks.get(task.taskId).run(task.target, arguments.taskArgs, inputs, project);
    }

    /// Collect transitive task dependency graph for given entry tasks.
    /// Each task computes own direct dependencies in [Task#findDependencies].
    private Map<TaskRequest, Set<TaskRequest>> collectTaskDependencies(Set<String> tasks, Set<Target> targets) throws IOException {
        var allDependencies = new TreeMap<TaskRequest, Set<TaskRequest>>();
        var requestQueue = (Queue<TaskRequest>) new LinkedList<TaskRequest>();
        for(var task : tasks) {
            if (! Tasks.getTasks().contains(task)) {
                throw CliErrors.UNKNOWN_TASK.exit(task, String.join(", ", Tasks.getTasks()));
            }
            for (var target : targets) {
                requestQueue.add(new TaskRequest(target, task));
            }
        }
        while (! requestQueue.isEmpty()) {
            var request = requestQueue.poll();
            if (allDependencies.containsKey(request)) {
                continue; // already processed
            }
            var task = Tasks.get(request.taskId);
            if (! task.isApplicable(project, request.target)) {
                continue;
            }
            var dependencies = Items.computeIfAbsent(taskDependencyCache, request, r -> task.findDependencies(project, r.target), IOException.class);
            allDependencies.put(request, dependencies);
            requestQueue.addAll(dependencies);
        }
        return allDependencies;
    }

    //region generated code
    public Baizel(
            Project project,
            Consumer<Issue> reporter
    ) {
        this.project = project;
        this.reporter = reporter;
    }
    //endregion
}
