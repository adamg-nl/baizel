package nl.adamg.baizel.internal.common.util.concurrent;

import nl.adamg.baizel.internal.common.util.Exceptions;
import nl.adamg.baizel.internal.common.util.functions.Runnable;

import javax.annotation.CheckForNull;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Runs submitted tasks in a thread pool.
 * @param <TException> checked exception that is allowed to be thrown from tasks
 * @see Executor#create public factory method
 */
public class ThreadPoolExecutor<TException extends Exception> implements Executor<TException> {
    private final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    private final List<Future<Void>> futures = new ArrayList<>();
    private final Instant startTime = Instant.now();
    private final ExecutorService executorService;
    private final Class<TException> exceptionType;
    private final Duration timeLimit;
    @CheckForNull private volatile Throwable taskException;

    public ThreadPoolExecutor(int threadCount, Class<TException> exceptionType, Duration timeLimit) {
        this(Executors.newFixedThreadPool(threadCount), exceptionType, timeLimit);
    }

    protected ThreadPoolExecutor(ExecutorService executorService, Class<TException> exceptionType, Duration timeLimit) {
        this.executorService = executorService;
        this.exceptionType = exceptionType;
        this.timeLimit = timeLimit;
    }

    /**
     * Runs the task in a thread pool.
     * If the task crashes with exception, best effort is made to stop processing of remaining tasks.
     */
    @Override
    public void run(@CheckForNull String threadName, Runnable<TException> task) {
        if (taskException != null) {
            return;
        }
        futures.add(executorService.submit(() -> {
            if (taskException != null) {
                return null;
            }
            var thread = Thread.currentThread();
            thread.setContextClassLoader(contextClassLoader);
            var originalName = thread.getName();
            if (threadName != null) {
                thread.setName(threadName + "[" + originalName + "]");
            }
            try {
                task.run();
            } catch (Throwable e) {
                synchronized (this) {
                    if (taskException == null) {
                        taskException = e;
                        // attempt to kill all the threads with InterruptedException
                        executorService.shutdownNow();
                    }
                }
                throw e;
            } finally {
                thread.setName(originalName);
            }
            return null;
        }));
    }

    public void close() throws TException, InterruptedException {
        executorService.shutdown();
        var elapsedTime = Duration.between(startTime, Instant.now());
        var remainingTime = timeLimit.minus(elapsedTime);
        var timedOut = ! executorService.awaitTermination(remainingTime.toSeconds(), TimeUnit.SECONDS);
        if (timedOut) {
            // attempt to kill all the threads with InterruptedException
            executorService.shutdownNow();
            throw new InterruptedException("timed out after " + timeLimit);
        }
        if (taskException != null) {
            throw Exceptions.rethrow(taskException, exceptionType, InterruptedException.class);
        }
        for (var future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                var cause = e.getCause();
                throw Exceptions.rethrow(cause != null ? cause : e, exceptionType, InterruptedException.class);
            }
        }
    }
}