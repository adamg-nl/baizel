package nl.adamg.baizel.core.model;

import nl.adamg.baizel.core.BaizelException;
import nl.adamg.baizel.core.TaskScheduler;
import nl.adamg.baizel.core.Tasks;
import nl.adamg.baizel.core.api.*;
import nl.adamg.baizel.core.api.BaizelOptions;
import nl.adamg.baizel.core.api.Invocation;
import nl.adamg.baizel.core.api.Project;
import nl.adamg.baizel.core.api.Target;
import nl.adamg.baizel.core.api.TaskRequest;
import nl.adamg.baizel.core.entities.BaizelErrors;
import nl.adamg.baizel.core.entities.Issue;
import nl.adamg.baizel.internal.common.io.FileSystem;
import nl.adamg.baizel.internal.common.io.Shell;
import nl.adamg.baizel.internal.common.util.LoggerUtil;
import nl.adamg.baizel.internal.common.util.collections.Items;
import nl.adamg.baizel.internal.common.util.java.typeref.TypeRef2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;

/// Baizel daemon for a project instance.
///
/// Can be used for multiple invocations.
/// Invocations are processed sequentially, but each of them may involve highly concurrent tasks.
///
/// - API:    [nl.adamg.baizel.core.api.Baizel]
/// - Model:  [nl.adamg.baizel.core.model.Baizel]
/// - CLI:    `nl.adamg.baizel.cli.Baizel`
public class Baizel implements nl.adamg.baizel.core.api.Baizel {
    private static final Logger LOG = Logger.getLogger(Baizel.class.getName());
    /// When Baizel instance is reused to run the same or overlapping set of tasks later,
    /// thanks to this map their task dependencies will not need to be recomputed.
    private final Map<TaskRequest, Set<TaskRequest>> taskDependencyCache;
    private final Project project;
    private final Consumer<Issue> reporter;
    private final FileSystem fileSystem;
    private final Shell shell;
    private final BaizelOptions options;
    private final Map<Invocation, TaskScheduler> runningInvocations = Collections.synchronizedMap(new TreeMap<>());

    //region factory
    public static nl.adamg.baizel.core.api.Baizel start(BaizelOptions options, Path projectRoot, Shell shell, FileSystem fileSystem, Consumer<Issue> reporter) throws IOException {
        return new Baizel(
                new ConcurrentHashMap<>(),
                nl.adamg.baizel.core.model.Project.load(projectRoot, reporter),
                reporter,
                fileSystem,
                shell,
                options
        );
    }
    //endregion

    @Override
    public synchronized void run(Invocation invocation) throws IOException, InterruptedException {
        if (invocation.tasks().isEmpty()) {
            throw new BaizelException(BaizelErrors.TASK_NOT_SELECTED);
        }
        var runningInvocation = runningInvocations.get(invocation);
        if (runningInvocation != null) {
            // exactly the same thing invoked on the same project - just wait till finished
            runningInvocation.waitUntilFinished();
            return;
        }
        try(var scheduler = TaskScheduler.create(options.workerCount(), getRunner(invocation))) {
            runningInvocations.put(invocation, scheduler);
            var taskDependencies = collectTaskDependencies(invocation.tasks(), invocation.targets());
            for(var dependency : taskDependencies.entrySet()) {
                LOG.info(() -> "scheduling" + LoggerUtil.with(
                        "task", dependency.getKey().toString(),
                        "dependencies", Items.toString(dependency.getValue(), " ", TaskRequest::toString)
                ));
                scheduler.schedule(dependency.getKey(), dependency.getValue());
            }
        }
        runningInvocations.remove(invocation);
    }

    @Override
    public void report(String issueId, String... details) {
        reporter.accept(new Issue(issueId, Items.map(new TypeRef2<>() {}, details)));
    }

    @Override
    public Target.Type getTargetType(Target target) {
        if (! target.artifact().isEmpty()) {
            return Target.Type.ARTIFACT;
        }
        if (target.path().isEmpty()) {
            return Target.Type.MODULE;
        }
        var moduleDefFile = nl.adamg.baizel.core.model.Module.getModuleDefinitionFile(project.path(target.path()));
        if(moduleDefFile != null) {
            return Target.Type.MODULE;
        }
        if (Files.exists(project.path(target.path()))) {
            return Target.Type.FILE;
        }
        return Target.Type.INVALID;
    }

    //region implementation internals
    private TaskScheduler.Runner getRunner(Invocation invocation) {
        return (task, inputs) -> Tasks.get(task.taskId()).run(task.target(), invocation.taskArgs(), inputs, getTargetType(task.target()), this);
    }

    /// Collect transitive task dependency graph for given entry tasks.
    /// Each task computes own direct dependencies in [Task#findDependencies].
    private Map<TaskRequest, Set<TaskRequest>> collectTaskDependencies(Set<String> tasks, Set<Target> targets) throws IOException {
        var allDependencies = new TreeMap<TaskRequest, Set<TaskRequest>>();
        var requestQueue = (Queue<TaskRequest>) new LinkedList<TaskRequest>();
        for(var task : tasks) {
            if (! Tasks.getTasks().contains(task)) {
                throw new BaizelException(BaizelErrors.UNKNOWN_TASK, task, String.join(", ", Tasks.getTasks()));
            }
            for (var target : targets) {
                requestQueue.add(nl.adamg.baizel.core.model.TaskRequest.of(target, task));
            }
        }
        while (! requestQueue.isEmpty()) {
            var request = requestQueue.poll();
            if (allDependencies.containsKey(request)) {
                continue; // already processed
            }
            var task = Tasks.get(request.taskId());
            var targetType = getTargetType(request.target());
            if (! task.isApplicable(request.target(), targetType, this)) {
                continue;
            }
            var dependencies = Items.computeIfAbsent(taskDependencyCache, request, r -> task.findDependencies(r.target(), targetType, this), IOException.class);
            allDependencies.put(request, dependencies);
            requestQueue.addAll(dependencies);
        }
        return allDependencies;
    }
    //endregion

    //region getters
    @Override
    public Consumer<Issue> reporter() {
        return reporter;
    }

    @Override
    public Project project() {
        return project;
    }

    @Override
    public BaizelOptions options() {
        return options;
    }

    @Override
    public FileSystem fileSystem() {
        return fileSystem;
    }

    @Override
    public Shell shell() {
        return shell;
    }
    //endregion

    //region generated code
    public Baizel(
            Map<TaskRequest, Set<TaskRequest>> taskDependencyCache,
            Project project,
            Consumer<Issue> reporter,
            FileSystem fileSystem,
            Shell shell ,
            BaizelOptions options
    ) {
        this.taskDependencyCache = taskDependencyCache;
        this.project = project;
        this.reporter = reporter;
        this.fileSystem = fileSystem;
        this.shell = shell;
        this.options = options;
    }
    //endregion
}
