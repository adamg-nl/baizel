package nl.adamg.baizel.core.entities;

public enum BaizelErrors {
    @SuppressWarnings("unused") // used in bin/baizel
    JDK_NOT_FOUND(201),
    @SuppressWarnings("unused") // used in Bootstrap.java
    BOOTSTRAP_COMPILATION_FAILED(202),
    TASK_NOT_SELECTED(203, "task not selected", true),
    UNKNOWN_TASK(204, "unknown task ${task}, available tasks: ${tasks}", true),
    UNRESOLVED_DEPENDENCIES(205, "unable to resolve dependencies: ${task}", false),
    INVALID_OPTION(206, "invalid CLI option provided: ${option}", true),
    TASK_FAILED(207, "task execution failed: ${task}", false),
    MODULE_NOT_FOUND(208, "module not found: ${module}", false),
    ARTIFACT_NOT_FOUND(209, "artifact not found: ${artifact}", false),
    INPUT_ISSUE(210, "input issue", false),
    COMPILATION_FAILED(211, "compilation failed: ${message} in ${file}:${lineNumber}", false),
    CYCLIC_DEPENDENCY(212, "cyclic dependency detected between tasks: ${tasks}", false),
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
