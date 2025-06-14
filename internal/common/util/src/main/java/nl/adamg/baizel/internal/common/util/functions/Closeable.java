package nl.adamg.baizel.internal.common.util.functions;

/// Alternative to AutoCloseable with more straightforward choice between
/// declaring checked exceptions or not. Use [Closeable.Safe] when not.
@FunctionalInterface
public interface Closeable<E extends Exception> extends AutoCloseable {
    @Override
    void close() throws E;

    interface Safe extends Closeable<RuntimeException> {
    }

}
