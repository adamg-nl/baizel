package nl.adamg.baizel.core.entities;

public enum BaizelErrors {
    @SuppressWarnings("unused") // used in bin/baizel
    JDK_NOT_FOUND(201),
    @SuppressWarnings("unused") // used in Bootstrap.java
    BOOTSTRAP_COMPILATION_FAILED(202),
    TASK_NOT_SELECTED(203, "task not selected", true),
    UNKNOWN_TASK(204, "unknown task ${1}, available tasks: ${2}", true),
    UNRESOLVED_DEPENDENCIES(205, "unable to resolve dependencies: ${1}", false),
    INVALID_OPTION(206, "invalid CLI option provided: ${1}", true),
    ;
    public final int exitCode;
    public final String message;
    public final boolean printUsage;

    BaizelErrors(int exitCode) {
        this.message = name();
        this.exitCode = exitCode;
        this.printUsage = false;
    }

    //region generated code
    BaizelErrors(int exitCode, String message, boolean printUsage) {
        this.message = message;
        this.exitCode = exitCode;
        this.printUsage = printUsage;
    }
    //endregion
}
