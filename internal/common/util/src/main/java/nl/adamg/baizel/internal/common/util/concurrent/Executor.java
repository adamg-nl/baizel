package nl.adamg.baizel.internal.common.util.concurrent;

import nl.adamg.baizel.internal.common.util.Exceptions;
import nl.adamg.baizel.internal.common.util.functions.Callable;

import javax.annotation.CheckForNull;
import java.time.Duration;

/**
 * Runs submitted tasks synchronously or in thread pool, depending on configured thread count.
 * @param <TException> checked exception that is allowed to be thrown from tasks.
 */
public abstract class Executor<TException extends Exception> implements AutoCloseable {
    /**
     * Runs the task synchronously or in a thread pool.
     * If the task crashes with exception, best effort is made to stop processing of remaining tasks.
     * @param threadName temporary name to set for current thread if thread pool was used
     */
    public abstract void run(@CheckForNull String threadName, Callable<Void, TException> task);

    public void run(Callable<Void, TException> task) {
        run(null, task);
    }

    /**
     * Blocks up to given timeout, until all the tasks are finished.
     * @throws TException if any of the tasks has thrown it
     * @throws RuntimeException if any of the tasks has thrown it
     * @throws InterruptedException if interrupted while waiting
     */
    @Override
    public abstract void close() throws TException, InterruptedException;

    /**
     * @param threadCount 1 for processing in current thread, more for thread pool
     * @param exceptionType {@link RuntimeException} if no checked exceptions are expected, exception class otherwise
     */
    public static <TException extends Exception> Executor<TException> create(
            int threadCount,
            Class<TException> exceptionType,
            @CheckForNull Duration timeLimit) {
        if (threadCount < 1) {
            throw new IllegalArgumentException("threadCount < 1");
        }
        if (timeLimit == null) {
            timeLimit = Duration.ofSeconds(Long.MAX_VALUE);
        }
        if (threadCount == 1) {
            return new CurrentThreadExecutor<>(exceptionType, timeLimit);
        }
        return new ThreadPoolExecutor<>(threadCount, exceptionType, timeLimit);
    }

    public static <TException extends Exception> Executor<TException> create(
            int threadCount,
            Class<TException> exceptionType) {
        return create(threadCount, exceptionType, null);
    }

    protected abstract Class<TException> getExceptionType();

    /**
     * Rethrows exception of unknown type without wrapping in RuntimeException.
     */
    protected void rethrow(@CheckForNull Throwable throwable) throws TException, InterruptedException {
        Exceptions.rethrowIfIs(throwable, getExceptionType());
        Exceptions.rethrowIfIs(throwable, InterruptedException.class);
        Exceptions.rethrowIfIs(throwable, RuntimeException.class);
        Exceptions.rethrowIfIs(throwable, Error.class);
        throw new RuntimeException(throwable);
    }
}
