package nl.adamg.baizel.internal.common.util.functions;

@SuppressWarnings("unused")
@FunctionalInterface
public interface ThrowingConsumer<T, E extends Exception> {
    void accept(T t) throws E;

    @FunctionalInterface
    interface Nullable<T, E extends Exception> {
        void accept(@javax.annotation.CheckForNull T t) throws E;
    }
}
