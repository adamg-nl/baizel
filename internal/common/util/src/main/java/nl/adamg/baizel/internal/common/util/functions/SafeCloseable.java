package nl.adamg.baizel.internal.common.util.functions;

@FunctionalInterface
public interface SafeCloseable extends AutoCloseable {
    @Override
    void close();
}
