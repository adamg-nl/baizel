package nl.adamg.baizel.internal.common.util;

import nl.adamg.baizel.internal.common.java.Methods;

public class LoggerUtil extends nl.adamg.baizel.internal.bootstrap.util.logging.LoggerUtil {
    private static final Lazy.NonNull.Safe<Boolean> IS_VERBOSE = Lazy.safeNonNull(() -> {
        String variable = System.getenv("BAIZEL_VERBOSE");
        return variable != null && ! variable.equals("false");
    });

    public static boolean isVerbose() {
        return IS_VERBOSE.get();
    }

    public static void logStackTrace() {
        if (! isVerbose()) {
            return;
        }
        System.err.println(Methods.getStackTrace());
    }
}
