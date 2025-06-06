package nl.adamg.baizel.internal.common.io;

import nl.adamg.baizel.internal.common.util.collections.Items;

import javax.annotation.CheckForNull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Utility for running shell commands and reading shell environment variables. As opposed to {@code
 * System.getenv()}, variables here can be overridden at runtime.
 */
public final class ShellUtil {
    private ShellUtil() {}

    public static Map<String, String> parseEnvMap(String envString) {
        var map = new HashMap<String, String>();
        for (var entry : envString.split("\n")) {
            var split = entry.split("=", 2);
            if (split.length != 2) {
                continue;
            }
            map.put(split[0], split[1]);
        }
        return map;
    }

    /**
     * @param inputs multiple colon-separated $PATH variables
     * @return $PATH variable without duplicates, keeping the same priority order as inputs
     */
    public static String joinEnvPath(String... inputs) {
        var output = new LinkedHashSet<String>();
        for (var input : inputs) {
            if (input == null) {
                continue;
            }
            output.addAll(Arrays.asList(input.split(":")));
        }
        return String.join(":", output);
    }

    @SafeVarargs
    public static String joinEnvPath(Map<String, String>... envs) {
        var paths = new ArrayList<String>();
        for (var env : envs) {
            var path = env.get("PATH");
            if (path != null) {
                paths.add(path);
            }
        }
        return joinEnvPath(paths.toArray(String[]::new));
    }

    public static String joinArgs(List<String> args) {
        return Items.collectionToString(args, " ", a -> "'" + a.replace("'", "\\'") + "'");
    }

    public enum LogLevel {
        DEBUG,
        INFO,
        WARN,
        ERROR
    }

    private static final Map<LogLevel, String> COLOR_CODES =
            Map.of(
                    LogLevel.WARN, "1;33",
                    LogLevel.ERROR, "1;31",
                    LogLevel.DEBUG, "2",
                    LogLevel.INFO, "2");


    public static ExecResult exec(String command) throws ExecException {
        return exec(command, new ShellConfig());
    }

    public static ExecResult exec(String command, ShellConfig config) throws ExecException {
        var jvmPwd = Path.of(".").toAbsolutePath();
        if (config.pwd == null) {
            config.pwd = jvmPwd;
        }
        if (!config.waitForExit) {
            command = "nohup " + command + " &>/dev/null & disown &>/dev/null";
        }
        if (config.log) {
            setColor(LogLevel.DEBUG);
            var relativePwd = jvmPwd.relativize(config.pwd).toString();
            if (!relativePwd.isEmpty()) {
                System.err.println("$ cd " + relativePwd);
            }
            System.err.println("$ " + command);
            setColor(null);
        }
        if (config.env == null) {
            config.env = Map.of();
        }
        if (config.timeout == null) {
            config.timeout = Duration.ZERO;
        }
        try {
            var envp =
                    config.env.entrySet().stream()
                            .map(entry -> entry.getKey() + "=" + entry.getValue())
                            .toArray(String[]::new);
            if (envp.length == 0) {
                envp = null;
            }
            var wrappedCommand = new String[] {"/bin/bash", "-c", command}; // required on Linux to search the $PATH
            var process = Runtime.getRuntime().exec(wrappedCommand, envp, config.pwd.toFile());
            var outBuffer = new ByteArrayOutputStream();
            var errBuffer = new ByteArrayOutputStream();
            process.getOutputStream().close(); // prevent it from waiting for input that never comes
            var out = possiblyForward(outBuffer, config.forwardingMode, false);
            var err = possiblyForward(errBuffer, config.forwardingMode, true);
            var outThread = transferStream(process.getInputStream(), out);
            var errThread = transferStream(process.getErrorStream(), err);
            if (config.timeout.isZero() || config.timeout.isNegative()) {
                process.waitFor();
                outThread.join();
                errThread.join();
            } else {
                process.waitFor(config.timeout.toMillis(), TimeUnit.MILLISECONDS);
                outThread.join(config.timeout.toMillis());
                errThread.join(config.timeout.toMillis());
            }
            var stdout = outBuffer.toString(StandardCharsets.UTF_8).strip();
            var stderr = errBuffer.toString(StandardCharsets.UTF_8).strip();
            return new ExecResult(process.exitValue(), stderr, stdout);
        } catch (InterruptedException | IOException e) {
            throw new ExecException(e);
        }
    }

    private static OutputStream possiblyForward(
            OutputStream buffer, @CheckForNull Shell.OutputForwardingMode forwardingMode, boolean isErr) {
        return switch (forwardingMode) {
            case ALL -> new TeeOutputStream(isErr ? System.err : System.out, buffer);
            case STDOUT -> !isErr ? new TeeOutputStream(System.out, buffer) : buffer;
            case ALL_TO_STDERR -> new TeeOutputStream(System.err, buffer);
            case STDERR -> isErr ? new TeeOutputStream(System.err, buffer) : buffer;
            case NONE -> buffer;
            case null -> buffer;
        };
    }

    public static void setColor(@CheckForNull LogLevel logLevel) {
        if (System.console() == null) {
            return;
        }
        if (logLevel == null) {
            System.err.print("\u001B[0m");
        } else {
            System.err.print("\u001B[" + COLOR_CODES.get(logLevel) + "m");
        }
        System.err.flush();
    }

    // region internal utils
    private static Thread transferStream(InputStream inStream, OutputStream outStream) {
        var thread =
                new Thread(
                        () -> {
                            try {
                                // transferTo may block until the stream is closed, so we need to run it in separate
                                // thread
                                inStream.transferTo(outStream);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
        thread.setDaemon(true); // don't wait for it if main thread exits
        thread.start();
        return thread;
    }

}
