package nl.adamg.baizel.internal.common.util;

import nl.adamg.baizel.internal.common.util.functions.Supplier;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * At first wraps an instruction how to retrieve some value. Once that value is requested for the
 * first time, it executes that initializer, disregards it, and keeps the value instead. On
 * subsequent calls, the same value will always be returned.
 */
public class Lazy<T, E extends Exception> {
    @CheckForNull private volatile T value;
    @CheckForNull private volatile Supplier.Nullable<T, E> initializer;

    public Lazy(Supplier.Nullable<T, E> initializer) {
        this.initializer = initializer;
    }

    public static <T, E extends Exception> Lazy<T, E> lazy(Supplier.Nullable<T, E> initializer) {
        return new Lazy<>(initializer);
    }

    public static <T> Safe<T> safe(java.util.function.Supplier<T> initializer) {
        return new Safe<>(initializer);
    }

    public static <T> NonNull.Safe<T> safeNonNull(java.util.function.Supplier<T> initializer) {
        return new NonNull.Safe<>(initializer);
    }

    public static <T, E extends Exception> NonNull<T, E> nonNull(java.util.function.Supplier<T> initializer) {
        return new NonNull<>(initializer::get);
    }

    public static class Safe<T> extends Lazy<T, RuntimeException> {
        public Safe(java.util.function.Supplier<T> initializer) {
            super(initializer::get);
        }
    }

    @CheckForNull
    public T get() throws E {
        if (initializer == null) {
            return value;
        }
        synchronized (this) {
            var localInitializer = initializer;
            if (localInitializer == null) {
                return value;
            }
            value = localInitializer.get();
            initializer = null; // also frees the objects that were only needed for initialization
            return value;
        }
    }

    public static class NonNull<T, E extends Exception> extends Lazy<T, E> {
        public NonNull(Supplier<T, E> initializer) {
            super(initializer::get);
        }

        public static class Safe<T> extends NonNull<T, RuntimeException> {
            public Safe(java.util.function.Supplier<T> initializer) {
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