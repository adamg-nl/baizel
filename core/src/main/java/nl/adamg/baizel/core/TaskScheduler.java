package nl.adamg.baizel.core;

import nl.adamg.baizel.core.tasks.TaskInput;
import nl.adamg.baizel.core.tasks.TaskRequest;
import nl.adamg.baizel.internal.common.util.LoggerUtil;
import nl.adamg.baizel.internal.common.util.collections.DirectedGraph;
import nl.adamg.baizel.internal.common.util.collections.Items;
import nl.adamg.baizel.internal.common.util.concurrent.Executor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class TaskScheduler implements AutoCloseable {
    private static final Logger LOG = Logger.getLogger(TaskScheduler.class.getName());
    /// parent: task that depends, children: tasks it depends on
    private final DirectedGraph<TaskRequest> dependencies = new DirectedGraph<>();
    /// as {@link #dependencies}, but tasks are removed from here as soon as they finish
    private final DirectedGraph<TaskRequest> remainingDependencies = new DirectedGraph<>();
    /// key: task that finished, value: list of output
    private final Map<TaskRequest, Set<Path>> finishedTasks = Collections.synchronizedMap(new TreeMap<>());
    private final Map<TaskRequest, Thread> runningTasks = Collections.synchronizedMap(new TreeMap<>());
    private final Executor<IOException> workerPool;
    private final Thread schedulerThread;
    private final Set<TaskRequest> readyTasks = Items.newConcurrentSet();
    private final Runner runner;

    public interface Runner {
        Set<Path> run(TaskRequest task, List<TaskInput> inputs);
    }

    /// Blocks until all the tasks are executed.
    public static void scheduleAndWait(Map<TaskRequest, Set<TaskRequest>> dependencies, int workerCount, Runner runner) throws IOException, InterruptedException {
        try(var scheduler = TaskScheduler.create(workerCount, runner)) {
            for(var dependency : dependencies.entrySet()) {
                var depsString = Items.toString(Items.flattenSet(dependencies.values()), " ", TaskRequest::toString);
                LOG.info("scheduling" + LoggerUtil.with("task", dependency.getKey().toString(), "dependencies", depsString));
                scheduler.schedule(dependency.getKey(), dependency.getValue());
            }
        }
    }

    /// Stops accepting new
    @Override
    public void close() throws IOException, InterruptedException {
        schedulerThread.interrupt();
        schedulerThread.join();
        workerPool.close();
    }

    //region internals
    /// Creates new task scheduler with given settings.
    private static TaskScheduler create(int workerCount, Runner runner) {
        var workerPool = Executor.create(workerCount, IOException.class);
        var schedulerThreadRunnable = new AtomicReference<Runnable>(); // solves circular dependency scheduler <> thread
        var schedulerThread = new Thread(() -> schedulerThreadRunnable.get().run());
        schedulerThread.setDaemon(true);
        var scheduler = new TaskScheduler(workerPool, schedulerThread, runner);
        schedulerThread.setName(TaskScheduler.class.getCanonicalName() + "#" + System.identityHashCode(scheduler));
        schedulerThreadRunnable.set(scheduler::schedulerThreadMain);
        schedulerThread.start();
        return scheduler;
    }

    /// Schedules a task
    private void schedule(TaskRequest task, Set<TaskRequest> dependencies) {
        if (this.dependencies.contains(task)) {
            return; // ignore attempts to schedule the same task twice
        }
        this.dependencies.add(task, dependencies);
        this.remainingDependencies.add(task, dependencies);
        if(dependencies.isEmpty()) {
            markTaskReady(task);
        }
    }

    /// Submits tasks to the workers, as soon as these tasks have their dependencies satisfied.
    private void schedulerThreadMain() {
        while (true) {
            List<TaskRequest> sortedReadyTasks;
            synchronized (readyTasks) {
                sortedReadyTasks = new ArrayList<>(readyTasks);
                readyTasks.clear();
            }
            // sort the ready tasks by how many other tasks still depend on them and submit for execution
            sortedReadyTasks.sort(Comparator.comparing(t -> -1 * remainingDependencies.parents(t).size()));
            for (var i = 0; i < sortedReadyTasks.size(); i++) {
                var task = sortedReadyTasks.get(i);
                workerPool.run(() -> workerThreadMain(task));
            }
            if (schedulerThread.isInterrupted()) {
                return;
            }
            synchronized (readyTasks) {
                try {
                    readyTasks.wait();
                } catch (InterruptedException e) {
                    schedulerThread.interrupt(); // do last round
                }
            }
        }
    }

    /// Runs the given task, if it didn't run yet, and then triggers successors
    private void workerThreadMain(TaskRequest task) {
        var thread = Thread.currentThread();
        thread.setName(task + " (" + thread.getName() + ")");

        // make sure this wasn't started yet
        synchronized (runningTasks) {
            if (runningTasks.containsKey(task) || finishedTasks.containsKey(task)) {
                return;
            }
            runningTasks.put(task, thread);
        }

        // run the task
        LOG.info("running" + LoggerUtil.with("task", task.toString()));
        var outputs = runner.run(task, collectInputs(task));
        LOG.info("finished" + LoggerUtil.with("task", task.toString()));

        // mark this task as finished, and other tasks that only depended on this one as ready
        finishedTasks.put(task, outputs);
        runningTasks.remove(task);
        var dependingTasks = remainingDependencies.parents(task);
        remainingDependencies.remove(task);
        for(var dependingTask : dependingTasks) {
            if (remainingDependencies.children(dependingTask).isEmpty()) {
                markTaskReady(dependingTask);
            }
        }
    }

    private void markTaskReady(TaskRequest task) {
        synchronized (readyTasks) {
            LOG.info("task ready to start" + LoggerUtil.with("task", task.toString()));
            readyTasks.add(task);
            readyTasks.notify();
        }
    }

    private List<TaskInput> collectInputs(TaskRequest task) {
        var inputs = new ArrayList<TaskInput>();
        for (var dependency : dependencies.children(task)) {
            inputs.add(new TaskInput(dependency.target, dependency.taskId, finishedTasks.get(dependency)));
        }
        return inputs;
    }
    //endregion

    //region generated code
    TaskScheduler(
            Executor<IOException> workerPool,
            Thread schedulerThread,
            Runner runner
    ) {
        this.workerPool = workerPool;
        this.schedulerThread = schedulerThread;
        this.runner = runner;
    }
    //endregion
}
