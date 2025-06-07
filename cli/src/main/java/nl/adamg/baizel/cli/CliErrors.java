package nl.adamg.baizel.cli;

import nl.adamg.baizel.internal.common.util.LoggerUtil;
import nl.adamg.baizel.internal.common.util.collections.Items;

@SuppressWarnings("unused")
public enum CliErrors {
    JDK_NOT_FOUND(201),
    PROJECT_ROOT_NOT_FOUND(202),
    TASK_NOT_SELECTED(203, "task not selected", true),
    UNKNOWN_TASK(204, "unknown task ${1}", true),
    ;
    private final int exitCode;
    private final String message;
    private final boolean printUsage;

    /// Use as `throw cliError.exit("details")` to convince the compiler that the program flow ends
    public RuntimeException exit(String... details) {
        System.err.println(processMessage(details));
        if (printUsage) {
            var usage = String.join("\n", Items.filter(Baizel.HELP.split("\n"), l -> l.startsWith("Usage: ")));
            System.err.println(usage);
        }
        LoggerUtil.logStackTrace();
        System.exit(exitCode);
        throw new RuntimeException("System.exit failed");
    }

    private String processMessage(String[] details) {
        var newMessage = message;
        for(var i=0; i<details.length; i++) {
            newMessage = newMessage.replace("${" + i + "}", details[i]);
        }
        return newMessage;
    }

    CliErrors(int exitCode) {
        this.message = name();
        this.exitCode = exitCode;
        this.printUsage = false;
    }

    //region generated code
    CliErrors(int exitCode, String message, boolean printUsage) {
        this.message = message;
        this.exitCode = exitCode;
        this.printUsage = printUsage;
    }

    public int exitCode() {
        return exitCode;
    }

    public String message() {
        return message;
    }
    //endregion
}
