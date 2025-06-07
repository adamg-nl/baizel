package nl.adamg.baizel.internal.common.util.functions;

/**
 * @see nl.adamg.baizel.internal.bootstrap.util.functions.Function
 */
@SuppressWarnings("unused")
public interface Function<T, R, E extends Exception> extends nl.adamg.baizel.internal.bootstrap.util.functions.Function<T, R, E> {
    @FunctionalInterface
    interface Nullable<T, R, E extends Exception> {
        @javax.annotation.CheckForNull
        R apply(@javax.annotation.CheckForNull T t) throws E;
    }
}
