package nl.adamg.baizel.internal.common.util.functions;

@FunctionalInterface
public interface Closeable<E extends Exception> extends AutoCloseable {
    @Override
    void close() throws E;

    interface Safe extends Closeable<RuntimeException> {
    }

}
