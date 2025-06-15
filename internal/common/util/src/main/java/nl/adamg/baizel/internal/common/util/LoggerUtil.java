package nl.adamg.baizel.internal.common.util;

import nl.adamg.baizel.internal.common.util.collections.Items;
import nl.adamg.baizel.internal.common.util.java.typeref.TypeRef2;

import java.util.Map;

public class LoggerUtil extends nl.adamg.baizel.internal.bootstrap.util.logging.LoggerUtil {
    private static final Lazy.NonNull.Safe<Boolean> IS_VERBOSE = Lazy.safeNonNull(() -> {
        String variable = System.getenv("BAIZEL_VERBOSE");
        return variable != null && ! variable.equals("false");
    });

    public static boolean isVerbose() {
        return IS_VERBOSE.get();
    }

    public static void logStackTraceIfVerbose() {
        if (! isVerbose()) {
            return;
        }
        new RuntimeException().printStackTrace(System.err);
    }

    /// Structured logging utility.
    /// Usage: `LOG.warning("message here" + LoggerUtil.with(Map.of("key", "value")))`
    public static String with(Map<String, String> details) {
        return " -- { " + Items.toString(details.entrySet(), ", ", LoggerUtil::toJson) + " }";
    }

    public static String with(String... details) {
        return with(Items.map(new TypeRef2<>(){}, details));
    }

    private static String toJson(Map.Entry<String, String> e) {
        return "\"" + sanitize(e.getKey()) + "\": \"" + sanitize(e.getValue()) + "\"";
    }

    private static String sanitize(String text) {
        return text.replaceAll("['\"\\\\]", "");
    }
}
