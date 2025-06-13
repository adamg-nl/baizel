package nl.adamg.baizel.internal.common.util.concurrent;

import java.time.Duration;
import java.util.concurrent.Executors;

public class VirtualThreadsExecutor<TException extends Exception> extends ThreadPoolExecutor<TException> {
    protected VirtualThreadsExecutor(Class<TException> exceptionType, Duration timeLimit) {
        super(Executors.newVirtualThreadPerTaskExecutor(), exceptionType, timeLimit);
    }
}
