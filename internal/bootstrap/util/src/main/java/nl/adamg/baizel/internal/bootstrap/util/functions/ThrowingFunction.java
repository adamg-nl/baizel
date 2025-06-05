package nl.adamg.baizel.internal.bootstrap.util.functions;

/**
 * Bootstrap part of {@code nl.adamg.baizel.internal.common.util.functions.ThrowingFunction}
 */
@SuppressWarnings("unused")
@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Exception> {
    R apply(T t) throws E;
}
