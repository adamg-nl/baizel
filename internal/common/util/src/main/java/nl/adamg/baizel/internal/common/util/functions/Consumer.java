package nl.adamg.baizel.internal.common.util.functions;

import javax.annotation.CheckForNull;

@SuppressWarnings("unused")
@FunctionalInterface
public interface Consumer<T, E extends Exception> {
    void accept(T t) throws E;

    @FunctionalInterface
    interface Nullable<T, E extends Exception> {
        void accept(@CheckForNull T t) throws E;
    }
}
