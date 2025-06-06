package nl.adamg.baizel.internal.common.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;

public final class DirEnvUtil {
    /**
     * @return entries added or changed by the first `.envrc` or `.env` file found upwards in the file tree.
     */
    public static Map<String, String> getDirEnv(Path directory, Shell shell) throws IOException {
        var dirEnv = new TreeMap<String, String>();
        var envFileNames = List.of(".envrc", ".env");
        boolean found = false;
        for (var parentDir : PathUtil.getAncestors(directory, true).reversed()) {
            for (var envFileName : envFileNames) {
                var envFile = parentDir.resolve(envFileName);
                if (!Files.exists(envFile)) {
                    continue;
                }
                found = true;
                var shellConfig = new ShellConfig();
                shellConfig.pwd = parentDir;
                var envString =
                        shell.exec("set -o allexport && source " + envFileName + " && env", shellConfig).stdOut();
                if (!envString.isEmpty()) {
                    dirEnv.putAll(ShellUtil.parseEnvMap(envString));
                }
            }
            if (found) {
                break;
            }
        }
        // skip entries that are unchanged compared to initial shell env
        var shellEnv = shell.getEnv();
        for (var key : new TreeSet<>(dirEnv.keySet())) {
            if (Objects.equals(dirEnv.get(key), shellEnv.get(key))) {
                dirEnv.remove(key);
            }
        }
        dirEnv.remove("SHLVL");
        dirEnv.remove("PWD");
        return dirEnv;
    }

    private DirEnvUtil() {}
}
