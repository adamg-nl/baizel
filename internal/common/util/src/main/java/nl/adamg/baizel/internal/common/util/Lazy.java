package nl.adamg.baizel.internal.common.util;

import nl.adamg.baizel.internal.common.util.functions.ThrowingSupplier;
import javax.annotation.CheckForNull;

/**
 * At first wraps an instruction how to retrieve some value. Once that value is requested for the
 * first time, it executes that initializer, disregards it, and keeps the value instead. On
 * subsequent calls, the same value will always be returned.
 */
public final class Lazy<T, E extends Exception> {
    @CheckForNull private volatile T value;
    @CheckForNull private volatile ThrowingSupplier<T, E> initializer;

    public Lazy(ThrowingSupplier<T, E> initializer) {
        this.initializer = initializer;
    }

    @CheckForNull
    public T get() throws E {
        if (initializer == null) {
            return value;
        }
        synchronized (this) {
            if (initializer == null) {
                return value;
            }
            value = initializer.get();
            initializer = null; // also frees objects only needed for initialization
            return value;
        }
    }
}