package nl.adamg.baizel.internal.common.util.concurrent;

import nl.adamg.baizel.internal.common.util.functions.Runnable;

import javax.annotation.CheckForNull;
import java.time.Duration;

/**
 * Runs submitted tasks synchronously or in thread pool, depending on configured thread count.
 * @param <TException> checked exception that is allowed to be thrown from tasks.
 */
public interface Executor<TException extends Exception> extends AutoCloseable {
    /**
     * Runs the task synchronously or in a thread pool.
     * If the task crashes with exception, best effort is made to stop processing of remaining tasks.
     * @param threadName temporary name to set for current thread if thread pool was used
     */
    void run(@CheckForNull String threadName, Runnable<TException> task);

    default void run(Runnable<TException> task) {
        run(null, task);
    }

    /**
     * Blocks up to given timeout, until all the tasks are finished.
     * @throws TException if any of the tasks has thrown it
     * @throws RuntimeException if any of the tasks has thrown it
     * @throws InterruptedException if interrupted while waiting
     */
    @Override
    void close() throws TException, InterruptedException;

    /**
     * @param threadCount 1 for processing in current thread, more for thread pool, -1 for unlimited virtual threads
     * @param exceptionType {@link RuntimeException} if no checked exceptions are expected, exception class otherwise
     */
    static <TException extends Exception> Executor<TException> create(
            int threadCount,
            Class<TException> exceptionType,
            @CheckForNull Duration timeLimit) {
        if (timeLimit == null) {
            timeLimit = Duration.ofSeconds(Long.MAX_VALUE);
        }
        if (threadCount == -1) {
            return new VirtualThreadsExecutor<>(exceptionType, timeLimit);
        }
        if (threadCount < 1) {
            throw new IllegalArgumentException("threadCount has to be -1 (unlimited), 1 (single) or 1+ (fixed pool)");
        }
        if (threadCount == 1) {
            return new CurrentThreadExecutor<>(exceptionType, timeLimit);
        }
        return new ThreadPoolExecutor<>(threadCount, exceptionType, timeLimit);
    }

    static <TException extends Exception> Executor<TException> create(
            int threadCount,
            Class<TException> exceptionType) {
        return create(threadCount, exceptionType, null);
    }

    /// Interrupts all the threads
    void interrupt();
}
