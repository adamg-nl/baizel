package nl.adamg.baizel.cli;

import nl.adamg.baizel.internal.common.util.LoggerUtil;

public enum CliErrors {
    @SuppressWarnings("unused") // used in bin/baizel
    JDK_NOT_FOUND(201),
    @SuppressWarnings("unused") // used in Bootstrap.java
    BOOTSTRAP_COMPILATION_FAILED(202),
    TASK_NOT_SELECTED(203, "task not selected", true),
    UNKNOWN_TASK(204, "unknown task ${1}, available tasks: ${2}", true),
    UNRESOLVED_DEPENDENCIES(205, "unable to resolve dependencies: ${1}", false),
    INVALID_OPTION(206, "invalid CLI option provided: ${1}", true),
    ;
    private final int exitCode;
    private final String message;
    private final boolean printUsage;

    /// Use as `throw cliError.exit("details")` to convince the compiler that the program flow ends
    public RuntimeException exit(String... details) {
        System.err.println(processMessage(details));
        if (printUsage) {
            var usage = "baizel [<BAIZEL_OPTION>...] <TASK>... [<TASK_ARG>...] [-- <TARGET>...]";
            for(var i=0; i<details.length; i++) {
                usage = usage.replace("${" + (i+1) + "}", details[i]);
            }
            System.err.println(usage);
        }
        LoggerUtil.logStackTraceIfVerbose();
        System.exit(exitCode);
        throw new RuntimeException("System.exit failed"); // should never happen
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
