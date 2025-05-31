package nl.adamg.baizel.internal.common.util.functions;

@SuppressWarnings("unused")
@FunctionalInterface
public interface ThrowingSupplier<T, E extends Exception> {
    T get() throws E;

    @FunctionalInterface
    interface Nullable<T, E extends Exception> {
        @javax.annotation.CheckForNull
        T get() throws E;
    }
}
