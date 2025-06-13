package nl.adamg.baizel.internal.common.util.concurrent;

import nl.adamg.baizel.internal.common.util.Exceptions;
import nl.adamg.baizel.internal.common.util.functions.Runnable;

import javax.annotation.CheckForNull;
import java.time.Duration;
import java.time.Instant;

/**
 * Processes submitted tasks synchronously.
 * @param <TException> checked exception that is allowed to be thrown from tasks.
 * @see Executor#create public factory method
 */
public class CurrentThreadExecutor<TException extends Exception> implements Executor<TException> {
    private final Class<TException> exceptionType;
    private final Duration timeLimit;
    private final Instant startTime;
    @CheckForNull private Throwable taskException;

    public CurrentThreadExecutor(Class<TException> exceptionType, Duration timeLimit){
        this.exceptionType = exceptionType;
        this.timeLimit = timeLimit;
        this.startTime = Instant.now();
    }

    /**
     * Runs the task synchronously.
     * If the task crashes with exception, remaining tasks will be ignored.
     */
    @Override
    public void run(@CheckForNull String threadName, Runnable<TException> task) {
        if (taskException != null) {
            return;
        }
        if (timedOut()) {
            taskException = new InterruptedException("timed out after " + timeLimit);
            return;
        }
        var thread = Thread.currentThread();
        var originalName = thread.getName();
        if (threadName != null) {
            thread.setName(threadName + "[" + originalName + "]");
        }
        try {
            task.run();
        } catch (Throwable e) {
            this.taskException = e;
        } finally {
            thread.setName(originalName);
        }
    }

    private boolean timedOut() {
        var timeElapsed = Duration.between(startTime, Instant.now());
        return timeElapsed.compareTo(timeLimit) > 0;
    }

    /**
     * Returns or throws immediately without blocking.
     * @throws TException if any of the tasks has thrown it
     * @throws InterruptedException if execution exceeded {@link #timeLimit}
     * @throws RuntimeException if any of the tasks has thrown it
     */
    @Override
    public void close() throws TException, InterruptedException {
        if (taskException != null) {
            throw Exceptions.rethrow(taskException, exceptionType, InterruptedException.class);
        }
    }
}