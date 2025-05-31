package nl.adamg.baizel.internal.common.util.functions;

@SuppressWarnings("unused")
@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Exception> {
    R apply(T t) throws E;

    @FunctionalInterface
    interface Nullable<T, R, E extends Exception> {
        @javax.annotation.CheckForNull
        R apply(@javax.annotation.CheckForNull T t) throws E;
    }
}
