package nl.adamg.baizel.internal.common.util;

import nl.adamg.baizel.internal.common.util.functions.ThrowingSupplier;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * At first wraps an instruction how to retrieve some value. Once that value is requested for the
 * first time, it executes that initializer, disregards it, and keeps the value instead. On
 * subsequent calls, the same value will always be returned.
 */
public class Lazy<T, E extends Exception> {
    @CheckForNull private volatile T value;
    @CheckForNull private volatile ThrowingSupplier<T, E> initializer;

    public Lazy(ThrowingSupplier<T, E> initializer) {
        this.initializer = initializer;
    }

    public static <T, E extends Exception> Lazy<T, E> lazy(ThrowingSupplier<T, E> initializer) {
        return new Lazy<>(initializer);
    }

    public static <T> Safe<T> safe(Supplier<T> initializer) {
        return new Safe<>(initializer);
    }

    public static <T> NonNull.Safe<T> safeNonNull(Supplier<T> initializer) {
        return new NonNull.Safe<>(initializer);
    }

    public static <T, E extends Exception> NonNull<T, E> nonNull(Supplier<T> initializer) {
        return new NonNull<>(initializer::get);
    }

    public static class Safe<T> extends Lazy<T, RuntimeException> {
        public Safe(Supplier<T> initializer) {
            super(initializer::get);
        }
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

    public static class NonNull<T, E extends Exception> extends Lazy<T, E> {
        public NonNull(ThrowingSupplier<T, E> initializer) {
            super(initializer);
        }

        public static class Safe<T> extends NonNull<T, RuntimeException> {
            public Safe(Supplier<T> initializer) {
                super(initializer::get);
            }
        }

        @Nonnull
        @Override
        public T get() throws E {
            return Objects.requireNonNull(super.get());
        }
    }
}