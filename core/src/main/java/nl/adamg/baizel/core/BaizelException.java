package nl.adamg.baizel.core;

import nl.adamg.baizel.core.entities.BaizelErrors;
import nl.adamg.baizel.internal.common.util.LoggerUtil;

public class BaizelException extends RuntimeException {
    private final BaizelErrors error;
    private final String[] details;

    /// @see [#exit()]
    public static BaizelException exit(BaizelErrors error, String... details) {
        throw new BaizelException(error, details).exit();
    }

    /// Logs the error message and exits the JVM process with configured exit code.
    /// Only logs the stacktrace if verbose mode is enabled.
    public BaizelException exit() {
        log();
        System.exit(error.exitCode);
        throw this;
    }

    public void log() {
        var processedMessage = error.message;
        for(var i=0; i<details.length; i++) {
            processedMessage = processedMessage.replace("${" + i + "}", details[i]);
        }
        System.err.println(processedMessage);
        if (error.printUsage) {
            var usage = "baizel [<BAIZEL_OPTION>...] <TASK>... [<TASK_ARG>...] [-- <TARGET>...]";
            for(var i = 0; i< details.length; i++) {
                usage = usage.replace("${" + (i+1) + "}", details[i]);
            }
            System.err.println(usage);
        }
        LoggerUtil.logStackTraceIfVerbose();
    }

    //region getters
    public BaizelErrors error() {
        return error;
    }

    //endregion

    //region generated code
    public BaizelException(BaizelErrors error, String... details) {
        this.error = error;
        this.details = details;
    }
    //endregion
}
