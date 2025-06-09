package nl.adamg.baizel.internal.common.util.functions;

@SuppressWarnings("unused")
@FunctionalInterface
public interface Callable<T, E extends Exception> {
    T call() throws E;
}
