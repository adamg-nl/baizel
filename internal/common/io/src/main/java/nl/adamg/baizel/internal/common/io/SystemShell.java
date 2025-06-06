package nl.adamg.baizel.internal.common.io;

import javax.annotation.CheckForNull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * The real implementation of {@link Shell}. Allows executing processes, reading and manipulating
 * the env.
 */
public final class SystemShell implements Shell {
    private final Map<String, String> env;

    public static Shell load(Path pwd) throws IOException {
        // we start with base Bash environment
        var cleanTerminalShellEnv = getCleanShellEnv();
        var env = new TreeMap<>(cleanTerminalShellEnv);
        var shell = new SystemShell(env);

        // then we use this to run any .envrc files in the hierarchy and we put resulting entries on top
        var currentDirEnv = DirEnvUtil.getDirEnv(pwd, shell);
        env.putAll(currentDirEnv);

        // then we put entries from this JVM process on top
        var currentJvmProcessEnv = System.getenv();
        env.putAll(currentJvmProcessEnv);

        // then we merge the $PATHs
        env.put("PATH", ShellUtil.joinEnvPath(currentJvmProcessEnv, currentDirEnv, cleanTerminalShellEnv));

        return shell;
    }

    public static Shell load() throws IOException {
        return load(Path.of(".").toAbsolutePath());
    }

    @Override
    public ExecResult exec(String command, ShellConfig config) throws ExecException {
        var processEnv = new TreeMap<>(env);
        if (config.env != null) {
            processEnv.putAll(config.env);
        }
        config.env = processEnv;
        return ShellUtil.exec(command, config);
    }

    @CheckForNull
    @Override
    public String getEnv(String key) {
        return env.get(key);
    }

    @Override
    public Map<String, String> getEnv() {
        return Collections.unmodifiableMap(env);
    }

    @Override
    public void overrideEnv(String key, @CheckForNull String value) {
        if (value == null) {
            env.remove(key);
        } else {
            env.put(key, value);
        }
    }

    // region internal utils
    private static Map<String, String> getCleanShellEnv() throws IOException {
        var envOverrides =
                "USER=" + System.getenv("USER") + " HOME=" + System.getenv("HOME") + " PWD=" + System.getenv("HOME");
        var getEnvCommand = "env -i " + envOverrides + " bash -l -c env";
        var shellConfig = new ShellConfig();
        shellConfig.pwd = Path.of(System.getenv("HOME"));
        var envString = ShellUtil.exec(getEnvCommand, shellConfig);
        return ShellUtil.parseEnvMap(envString.stdOut());
    }

    private Path getPwd(@CheckForNull Path argumentOverride, @CheckForNull Map<String, String> envOverrides) {
        if (argumentOverride != null) {
            return argumentOverride;
        }
        if (envOverrides != null) {
            var envOverride = envOverrides.get("PWD");
            if (envOverride != null) {
                return Path.of(envOverride);
            }
        }
        var envPwd = env.get("PWD");
        if (envPwd != null) {
            return Path.of(envPwd);
        }
        return Path.of(".").toAbsolutePath();
    }

    // endregion

    // region generated code
    public SystemShell(Map<String, String> env) {
        this.env = env;
    }
    // endregion
}
