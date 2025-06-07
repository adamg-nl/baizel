package nl.adamg.baizel.internal.common.java;

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.annotation.CheckForNull;

public final class Exceptions {
    /**
     * Rethrows given exception, wrapping it in {@link RuntimeException} if it was neither {@link
     * RuntimeException} nor {@link Error}. Usage: {@code throw rethrow(e);}
     */
    public static RuntimeException rethrow(Throwable throwable) {
        return rethrow(throwable, RuntimeException.class);
    }

    public static <T extends Exception> RuntimeException rethrow(Throwable throwable, Class<T> allowedType) throws T {
        if (allowedType.isInstance(throwable)) {
            throw allowedType.cast(throwable);
        }
        if (throwable instanceof RuntimeException) {
            throw (RuntimeException) throwable;
        }
        if (throwable instanceof Error) {
            throw (Error) throwable;
        }
        throw new RuntimeException(throwable);
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
