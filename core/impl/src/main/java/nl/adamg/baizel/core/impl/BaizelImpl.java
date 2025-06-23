package nl.adamg.baizel.core.impl;

import java.util.Collections;
import nl.adamg.baizel.core.api.TaskScheduler;
import nl.adamg.baizel.core.api.Baizel;
import nl.adamg.baizel.core.api.BaizelOptions;
import nl.adamg.baizel.core.api.Invocation;
import nl.adamg.baizel.core.api.Project;
import nl.adamg.baizel.core.api.TargetCoordinates;
import nl.adamg.baizel.core.api.Task;
import nl.adamg.baizel.core.api.TaskRequest;
import nl.adamg.baizel.core.entities.BaizelErrors;
import nl.adamg.baizel.internal.common.io.FileSystem;
import nl.adamg.baizel.internal.common.io.Shell;
import nl.adamg.baizel.internal.common.util.LoggerUtil;
import nl.adamg.baizel.internal.common.util.collections.Items;
import nl.adamg.baizel.internal.common.util.concurrent.Executor;
import nl.adamg.baizel.internal.common.util.java.typeref.TypeRef2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.logging.Logger;

/// Baizel daemon for a project instance.
///
/// Can be used for multiple invocations.
/// Invocations are processed sequentially, but each of them may involve highly concurrent tasks.
///
/// - API:    [nl.adamg.baizel.core.api.Baizel]
/// - Impl:   [nl.adamg.baizel.core.impl.BaizelImpl]
/// - CLI:    [nl.adamg.baizel.cli.Baizel]
@SuppressWarnings("JavadocReference")
public class BaizelImpl implements Baizel {
    private static final Logger LOG = Logger.getLogger(BaizelImpl.class.getName());
    /// When Baizel instance is reused to run the same or overlapping set of tasks later,
    /// thanks to this map their task dependencies will not need to be recomputed.
    private final Map<TaskRequest, Set<TaskRequest>> taskDependencyCache = Collections.synchronizedMap(new TreeMap<>());
    private final Executor<IOException> workerPool;
    private final Project project;
    private final Consumer<nl.adamg.baizel.core.entities.Issue> reporter;
    private final FileSystem fileSystem;
    private final Shell shell;
    private final BaizelOptions options;

    //region factory
    public static Baizel start(BaizelOptions options, Path projectRoot, Shell shell, FileSystem fileSystem, Consumer<nl.adamg.baizel.core.entities.Issue> reporter) throws IOException {
        return new BaizelImpl(
                ProjectImpl.load(projectRoot, shell, fileSystem, reporter),
                Executor.create(options.workerCount(), IOException.class),
                fileSystem,
                shell,
                options,
                reporter
        );
    }
    //endregion

    @Override
    public synchronized void run(Invocation invocation) throws IOException, InterruptedException {
        if (invocation.tasks().isEmpty()) {
            throw nl.adamg.baizel.core.impl.Issue.critical(BaizelErrors.TASK_NOT_SELECTED);
        }
        try(var scheduler = TaskSchedulerImpl.create(workerPool, getRunner(invocation))) {
            var taskDependencies = collectTaskDependencies(invocation.tasks(), invocation.targets());
            for(var dependency : taskDependencies.entrySet()) {
                LOG.info(() -> "scheduling" + LoggerUtil.with(
                        "task", dependency.getKey().toString(),
                        "dependencies", Items.toString(dependency.getValue(), " ", TaskRequest::toString)
                ));
                scheduler.schedule(dependency.getKey(), dependency.getValue());
            }
        }
    }

    @Override
    public void report(String issueId, String messageTemplate, String... details) {
        reporter.accept(new nl.adamg.baizel.core.entities.Issue(
                issueId,
                BaizelErrors.INPUT_ISSUE.exitCode,
                Items.map(new TypeRef2<>() {}, details),
                messageTemplate
        ));
    }

    @Override
    public TargetCoordinates.CoordinateKind getTargetType(TargetCoordinates target) {
        if (! target.artifact().isEmpty()) {
            return TargetCoordinates.CoordinateKind.ARTIFACT;
        }
        if (target.path().isEmpty()) {
            return TargetCoordinates.CoordinateKind.MODULE;
        }
        var moduleDefFile = ModuleImpl.getModuleDefinitionFile(project.path(target.path()));
        if(moduleDefFile != null) {
            return TargetCoordinates.CoordinateKind.MODULE;
        }
        if (Files.exists(project.path(target.path()))) {
            return TargetCoordinates.CoordinateKind.FILE;
        }
        return TargetCoordinates.CoordinateKind.INVALID;
    }

    @Override
    public void close() throws IOException, InterruptedException {
        workerPool.close();
    }

    @Override
    public String toString() {
        return "baizel " + options;
    }

    //region implementation internals
    private TaskScheduler.Runner<TaskRequest> getRunner(Invocation invocation) {
        return (task, inputs) -> Tasks.get(task.taskId()).run(task.target(), invocation.taskArgs(), inputs, getTargetType(task.target()), this);
    }

    /// Collect transitive task dependency graph for given entry tasks.
    /// Each task computes own direct dependencies in [Task#findDependencies].
    private Map<TaskRequest, Set<TaskRequest>> collectTaskDependencies(Set<String> tasks, Set<TargetCoordinates> targets) throws IOException {
        var allDependencies = new TreeMap<TaskRequest, Set<TaskRequest>>();
        var requestQueue = (Queue<TaskRequest>) new LinkedList<TaskRequest>();
        for(var task : tasks) {
            if (! Tasks.getTasks().contains(task)) {
                throw nl.adamg.baizel.core.impl.Issue.critical(BaizelErrors.UNKNOWN_TASK, "task", task, "tasks", String.join(", ", Tasks.getTasks()));
            }
            for (var target : targets) {
                requestQueue.add(TaskRequestImpl.of(target, task));
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
    public Consumer<nl.adamg.baizel.core.entities.Issue> reporter() {
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
    public BaizelImpl(
            Project project,
            Executor<IOException> workerPool,
            FileSystem fileSystem,
            Shell shell,
            BaizelOptions options,
            Consumer<nl.adamg.baizel.core.entities.Issue> reporter
    ) {
        this.workerPool = workerPool;
        this.project = project;
        this.reporter = reporter;
        this.fileSystem = fileSystem;
        this.shell = shell;
        this.options = options;
    }
    //endregion
}
