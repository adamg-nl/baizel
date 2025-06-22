package nl.adamg.baizel.internal.bootstrap.util.functions;

@SuppressWarnings("unused")
@FunctionalInterface
public interface Predicate<T, E extends Exception> {
    boolean test(T t) throws E;
}
