package nl.adamg.baizel.internal.common.util.functions;

@SuppressWarnings("unused")
@FunctionalInterface
public interface Predicate<T, E extends Exception> {
    boolean test(T t) throws E;

    @FunctionalInterface
    interface Nullable<T, E extends Exception> {
        boolean test(@javax.annotation.CheckForNull T t) throws E;
    }
}
