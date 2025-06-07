package nl.adamg.baizel.internal.common.java;

public final class Methods {
    public static String getStackTrace() {
        return Exceptions.getStackTrace(new Throwable());
    }

    private Methods() {}
}
