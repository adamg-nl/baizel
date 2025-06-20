package nl.adamg.baizel.internal.common.util.functions;

import javax.annotation.CheckForNull;

@SuppressWarnings("unused")
@FunctionalInterface
public interface Supplier<T, E extends Exception> {
    T get() throws E;

    @FunctionalInterface
    interface Nullable<T, E extends Exception> {
        @CheckForNull
        T get() throws E;
    }
}
