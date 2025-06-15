module nl.adamg.baizel.cli {
    exports nl.adamg.baizel.cli;
    exports nl.adamg.baizel.cli.internal;

    uses nl.adamg.baizel.core.tasks.Task;

    provides nl.adamg.baizel.core.tasks.Task with nl.adamg.baizel.cli.tasks.Compile;

    requires transitive nl.adamg.baizel.internal.common.annotations;
    requires transitive nl.adamg.baizel.core;
     requires java.logging;
    requires nl.adamg.baizel.internal.common.util;
    requires nl.adamg.baizel.internal.common.io;
    requires nl.adamg.baizel.internal.bootstrap.util;
    requires nl.adamg.baizel.internal.common.java;
    requires nl.adamg.baizel.internal.jar;
    requires nl.adamg.baizel.internal.common.serialization;
}
