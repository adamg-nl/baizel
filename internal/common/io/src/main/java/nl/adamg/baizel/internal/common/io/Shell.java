package nl.adamg.baizel.internal.common.io;

import javax.annotation.CheckForNull;
import java.util.Map;

/**
 * Unit-testable abstraction for a system shell
 *
 * @see SystemShell
 */
public interface Shell {
    enum OutputForwardingMode {
        NONE,
        ALL,
        STDOUT,
        STDERR,
        ALL_TO_STDERR
    }

    ExecResult exec(String command, ShellConfig config) throws ExecException;

    @CheckForNull
    String getEnv(String key);

    Map<String, String> getEnv();

    /**
     * Does not actually modify the system env of the current JVM process, instead only affects the
     * env as seen by this instance and processes started with it.
     */
    void overrideEnv(String key, @CheckForNull String value);

    // region default utils
    default ExecResult exec(String command) throws ExecException {
        return exec(command, new ShellConfig());
    }

    default void unsetEnv(String key) {
        overrideEnv(key, null);
    }

    default void unsetEnv(String... keys) {
        for (var key : keys) {
            unsetEnv(key);
        }
    }

    default void overrideEnv(Map<String, String> overrides) {
        for (var entry : overrides.entrySet()) {
            overrideEnv(entry.getKey(), entry.getValue());
        }
    }
    // endregion
}
