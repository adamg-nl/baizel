package nl.adamg.baizel.internal.common.util.concurrent;

import nl.adamg.baizel.internal.common.util.functions.Callable;

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
public class ThreadPoolExecutor<TException extends Exception> extends Executor<TException> {
    private final ExecutorService executorService;
    private final List<Future<Void>> futures;
    private final Class<TException> exceptionType;
    private final Duration timeLimit;
    private final Instant startTime;
    @CheckForNull private volatile Throwable taskException;

    public ThreadPoolExecutor(int threadCount, Class<TException> exceptionType, Duration timeLimit) {
        this.executorService = Executors.newFixedThreadPool(threadCount);
        this.exceptionType = exceptionType;
        this.timeLimit = timeLimit;
        this.futures = new ArrayList<>();
        this.startTime = Instant.now();
    }

    /**
     * Runs the task in a thread pool.
     * If the task crashes with exception, best effort is made to stop processing of remaining tasks.
     */
    @Override
    public void run(@CheckForNull String threadName, Callable<Void, TException> task) {
        if (taskException != null) {
            return;
        }
        futures.add(executorService.submit(() -> {
            if (taskException != null) {
                return null;
            }
            var thread = Thread.currentThread();
            var originalName = thread.getName();
            if (threadName != null) {
                thread.setName(threadName + "[" + originalName + "]");
            }
            try {
                task.call();
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
            rethrow(taskException);
        }
        for (var future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                var cause = e.getCause();
                rethrow(cause != null ? cause : e);
            }
        }
    }

    @Override
    protected Class<TException> getExceptionType() {
        return exceptionType;
    }
}