package nl.adamg.baizel.internal.common.util.concurrent;

import nl.adamg.baizel.internal.common.util.functions.Closeable;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/// Auto-closeable utility wrapping [ReadWriteLock].
/// Frees the lock at the end of the `try { }` block.
///
/// Usage:
/// ```
/// try(var ignoredLock = lock.read()) {
///     ...
/// }
/// ```
public class Lock {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public Closeable.Safe read() {
        return lock(this.lock.readLock());
    }

    public Closeable.Safe write() {
        return lock(this.lock.writeLock());
    }

    private static Closeable.Safe lock(java.util.concurrent.locks.Lock lock) {
        lock.lock();
        return lock::unlock;
    }
}
