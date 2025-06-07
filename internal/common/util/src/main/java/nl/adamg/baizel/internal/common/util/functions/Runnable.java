package nl.adamg.baizel.internal.common.util.functions;

@SuppressWarnings("unused")
@FunctionalInterface
public interface Runnable<E extends Exception> {
    void run() throws E;
}
