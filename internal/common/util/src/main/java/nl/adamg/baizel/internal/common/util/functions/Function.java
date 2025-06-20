package nl.adamg.baizel.internal.common.util.functions;

import javax.annotation.CheckForNull;

/**
 * @see nl.adamg.baizel.internal.bootstrap.util.functions.Function
 */
@SuppressWarnings("unused")
public interface Function<T, R, E extends Exception> extends nl.adamg.baizel.internal.bootstrap.util.functions.Function<T, R, E> {
    @FunctionalInterface
    interface Nullable<T, R, E extends Exception> {
        @CheckForNull
        R apply(@CheckForNull T t) throws E;
    }
}
