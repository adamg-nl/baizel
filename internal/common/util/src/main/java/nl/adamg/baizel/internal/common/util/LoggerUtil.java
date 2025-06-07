package nl.adamg.baizel.internal.common.util;

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
        new RuntimeException().printStackTrace(System.err);
    }
}
