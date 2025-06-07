package nl.adamg.baizel.internal.common.java;

import nl.adamg.baizel.internal.common.util.Exceptions;

public final class Methods {
    public static String getStackTrace() {
        return Exceptions.getStackTrace(new Throwable());
    }

    private Methods() {}
}
