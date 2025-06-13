package nl.adamg.baizel.internal.common.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.annotation.CheckForNull;

public final class Exceptions {
    /**
     * Rethrows given exception, wrapping it in {@link RuntimeException} if it was neither {@link
     * RuntimeException} nor {@link Error}. Usage: {@code throw rethrow(e);}
     */
    public static RuntimeException rethrow(Throwable throwable) {
        throw rethrow(throwable, RuntimeException.class);
    }

    /**
     * Rethrows exception of unknown type without wrapping in RuntimeException.
     */
    public static <TException extends Exception> RuntimeException rethrow(@CheckForNull Throwable throwable, Class<TException> expectedType) throws TException {
        rethrowIfIs(throwable, expectedType);
        rethrowIfIs(throwable, RuntimeException.class);
        rethrowIfIs(throwable, Error.class);
        throw new RuntimeException(throwable);
    }

    public static <TException1 extends Exception, TException2 extends Exception> RuntimeException rethrow(@CheckForNull Throwable throwable, Class<TException1> expectedType1, Class<TException2> expectedType2) throws TException1, TException2 {
        rethrowIfIs(throwable, expectedType1);
        throw rethrow(throwable, expectedType2);
    }

    public static <TException1 extends Exception, TException2 extends Exception, TException3 extends Exception> RuntimeException rethrow(@CheckForNull Throwable throwable, Class<TException1> expectedType1, Class<TException2> expectedType2, Class<TException3> expectedType3) throws TException1, TException2, TException3 {
        rethrowIfIs(throwable, expectedType1);
        rethrowIfIs(throwable, expectedType2);
        throw rethrow(throwable, expectedType3);
    }

    /**
     * Rethrows given exception if it belongs to the specified type.
     */
    public static <E extends Throwable> void rethrowIfIs(@CheckForNull Throwable throwable, Class<E> ofType) throws E {
        if (ofType.isInstance(throwable)) {
            if (throwable instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw ofType.cast(throwable);
        }
    }

    /**
     * Throws passed exception, if any was passed. If exception was checked, it will be wrapped in a
     * {@link RuntimeException}. {@link RuntimeException}s and {@link Error}s are thrown unwrapped.
     */
    public static void rethrowIfAny(@CheckForNull Throwable throwable) {
        rethrowIfAny(throwable, RuntimeException.class);
    }

    public static <T extends Exception> void rethrowIfAny(@CheckForNull Throwable throwable, Class<T> allowedType)
            throws T {
        if (throwable == null) {
            return;
        }
        throw rethrow(throwable, allowedType);
    }

    /** Usage: {@code throw interrupt(e);} */
    public static InterruptedException interrupt(Throwable cause) throws InterruptedException {
        var interrupt = new InterruptedException("cancelled");
        interrupt.initCause(cause);
        Thread.currentThread().interrupt();
        throw interrupt;
    }

    public static String getStackTrace(Throwable throwable) {
        var writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    private Exceptions() {}
}
