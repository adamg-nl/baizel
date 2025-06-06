package nl.adamg.baizel.internal.common.io;

import javax.annotation.CheckForNull;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

public final class ShellConfig {
    @CheckForNull public Path pwd;
    @CheckForNull public Map<String, String> env;
    @CheckForNull public Duration timeout;
    @CheckForNull
    public Shell.OutputForwardingMode forwardingMode;
    boolean log = false;
    boolean waitForExit = true;
}
