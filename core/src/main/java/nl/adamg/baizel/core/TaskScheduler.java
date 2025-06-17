package nl.adamg.baizel.core;

import java.util.TreeSet;
import nl.adamg.baizel.internal.common.util.Exceptions;
import nl.adamg.baizel.internal.common.util.LoggerUtil;
import nl.adamg.baizel.internal.common.util.collections.DirectedGraph;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/// [#schedule] accepts a pair of an abstract task and it's dependencies.
///
/// Tasks must be comparable: it's used to tell if two tasks are the same task, and if not, which
/// one should be considered first in absence of other prioritization factors.
///
/// This object is AutoCloseable, and closing it blocks until all the scheduled tasks are finished:
/// ```
/// try(var executor = Executor.create(8, IOException.class);
///     var scheduler = TaskScheduler.create(executor, Task::run)) {
///     scheduler.schedule(new Task(...));
///     scheduler.schedule(new Task(...));
///     scheduler.schedule(new Task(...));
/// }
/// // this line will be only reached once all the tasks finished, in any order
/// ```
public class TaskScheduler<Task extends Comparable<Task>> implements AutoCloseable {
    private static final Logger LOG = Logger.getLogger(TaskScheduler.class.getName());
    /// parent: task that depends, children: tasks it depends on
    private final DirectedGraph<Task> allTasksAndDependencies = new DirectedGraph<>();
    /// like {@link #allTasksAndDependencies}, but tasks are removed from here as soon as they finish
    private final DirectedGraph<Task> unfinishedTasks = new DirectedGraph<>();
    /// key: task that finished, value: list of output files
    private final Map<Task, Set<Path>> finishedTasks = Collections.synchronizedMap(new TreeMap<>());
    private final Map<Task, Thread> runningTasks = Collections.synchronizedMap(new TreeMap<>());
    /// tasks that can be already started, because all their dependency inputs are ready
    private final Set<Task> readyTasks = Collections.synchronizedSet(new TreeSet<>());
    private final AtomicBoolean isShuttingDown = new AtomicBoolean(false);
    private final AtomicReference<Throwable> taskException = new AtomicReference<>();
    private final Object waiter = new Object();
    private final Executor<IOException> workerPool;
    private final Thread schedulerThread;
    private final Runner<Task> runner;

    /// How to run the task, given the list of inputs?
    @FunctionalInterface
    public interface Runner<Task extends Comparable<Task>> {
        Set<Path> run(Task task, List<Input<Task>> inputs) throws IOException;
    }

    public record Input<Task extends Comparable<Task>>(Task source, Set<Path> paths) {}

    public static <Task extends Comparable<Task>> TaskScheduler<Task> create(Executor<IOException> workerPool, Runner<Task> runner) {
        var schedulerThreadRunnable = new Runnable[1]; // solves circular dependency scheduler <> thread
        var schedulerThread = new Thread(() -> schedulerThreadRunnable[0].run());
        var scheduler = new TaskScheduler<>(workerPool, schedulerThread, runner);
        schedulerThread.setName(TaskScheduler.class.getCanonicalName() + "#" + System.identityHashCode(scheduler));
        schedulerThreadRunnable[0] = scheduler::schedulerThreadMain;
        schedulerThread.start();
        return scheduler;
    }

    /// Schedules a task
    public synchronized void schedule(Task task, Set<Task> dependencies) {
        if (isShuttingDown.get()) {
            throw new IllegalStateException("shutting down");
        }
        if (this.allTasksAndDependencies.contains(task)) {
            throw new IllegalStateException("task already scheduled");
        }
        this.allTasksAndDependencies.add(task, dependencies);
        this.unfinishedTasks.add(task, dependencies);
        if(dependencies.isEmpty()) {
            markTaskReady(task);
        }
    }

    @Override
    public void close() throws IOException, InterruptedException {
        LOG.info("waiting for scheduled tasks to finish");
        isShuttingDown.set(true);
        headsUp();
        schedulerThread.join();
        while (! unfinishedTasks.isEmpty() && taskException.get() == null) {
            waitABit();
        }
        if (taskException.get() == null) {
            LOG.info("shutdown finished");
        } else {
            LOG.info("crash finished");
        }
        Exceptions.rethrowIfIs(taskException.get(), IOException.class);
        Exceptions.rethrowIfIs(taskException.get(), InterruptedException.class);
        Exceptions.rethrowIfAny(taskException.get());
    }

    public void interrupt() {
        LOG.warning("interrupting all threads");
        for(var thread : runningTasks.values()) {
            thread.interrupt();
        }
    }
    //region internals

    /// waits for short time or until something interesting happens
    private void waitABit() throws InterruptedException {
        synchronized (waiter) {
            waiter.wait(100);
        }
    }

    /// signals waiting threads that something interesting has happened
    private void headsUp() {
        synchronized (waiter) {
            waiter.notifyAll();
        }
    }

    /// Submits tasks to the workers, as soon as these tasks have their dependencies satisfied.
    private void schedulerThreadMain() {
        while (true) {
            List<Task> sortedReadyTasks;
            synchronized (readyTasks) {
                sortedReadyTasks = new ArrayList<>(readyTasks);
                readyTasks.clear();
            }
            // sort the ready tasks by how many other tasks still depend on them and submit for execution
            sortedReadyTasks.sort(Comparator.<Task, Integer>comparing(task -> -1 * unfinishedTasks.parents(task).size()).thenComparing(task -> task));
            for (var task : sortedReadyTasks) {
                workerPool.run(() -> workerThreadMain(task));
            }
            if (schedulerThread.isInterrupted()) {
                LOG.severe("scheduler thread interrupted");
                taskException.compareAndSet(null, new InterruptedException());
                return;
            }
            if (isShuttingDown.get() && unfinishedTasks.isEmpty()) {
                LOG.info("scheduler thread finished");
                return;
            }
            if (taskException.get() != null) {
                LOG.warning("scheduler exiting due to task crash");
                return;
            }
            if (! readyTasks.isEmpty()) {
                continue;
            }
            try {
                while (readyTasks.isEmpty()) {
                    if (taskException.get() != null) {
                        LOG.warning("scheduler exiting due to task crash");
                        return;
                    }
                    waitABit();
                }
            } catch (InterruptedException e) {
                LOG.severe("scheduler thread interrupted");
                taskException.compareAndSet(null, e);
                return;
            }
        }
    }

    /// Runs the given task, if it didn't run yet, and then triggers successors
    private void workerThreadMain(Task task) {
        var thread = Thread.currentThread();
        thread.setName(task + " (" + thread.getName() + ")");
        Set<Path> outputs;

        // run the task
        LOG.info("running" + LoggerUtil.with("task", task.toString()));
        try {
            outputs = runner.run(task, collectInputs(task));
        } catch (Throwable t) {
            taskException.compareAndSet(null, t);
            headsUp();
            LOG.severe("task crashed" + LoggerUtil.with(
                    "task", task.toString(),
                    "exceptionClass", t.getClass().getName(),
                    "exception", t.toString()
            ));
            this.interrupt();
            return;
        }
        LOG.info("finished" + LoggerUtil.with("task", task.toString()));

        // mark this task as finished, and other tasks that only depended on this one as ready
        finishedTasks.put(task, outputs);
        runningTasks.remove(task);
        var dependingTasks = unfinishedTasks.parents(task);
        unfinishedTasks.remove(task);
        for(var dependingTask : dependingTasks) {
            if (unfinishedTasks.children(dependingTask).isEmpty()) {
                markTaskReady(dependingTask);
            }
        }
        headsUp();
    }

    private void markTaskReady(Task task) {
        LOG.info("task ready to start" + LoggerUtil.with("task", task.toString()));
        synchronized (readyTasks) {
            readyTasks.add(task);
        }
        headsUp();
    }

    private List<Input<Task>> collectInputs(Task task) {
        var inputs = new ArrayList<Input<Task>>();
        for (var dependency : allTasksAndDependencies.children(task)) {
            inputs.add(new Input<>(dependency, finishedTasks.get(dependency)));
        }
        return inputs;
    }
    //endregion

    //region generated code
    TaskScheduler(
            Executor<IOException> workerPool,
            Thread schedulerThread,
            Runner<Task> runner
    ) {
        this.workerPool = workerPool;
        this.schedulerThread = schedulerThread;
        this.runner = runner;
    }
    //endregion
}
