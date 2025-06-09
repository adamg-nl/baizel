module nl.adamg.baizel.cli {
    exports nl.adamg.baizel.cli;
    exports nl.adamg.baizel.cli.internal;

    uses nl.adamg.baizel.cli.internal.Task;

    provides nl.adamg.baizel.cli.internal.Task with nl.adamg.baizel.cli.tasks.Build, nl.adamg.baizel.cli.tasks.Compile, nl.adamg.baizel.cli.tasks.Jar;

    requires transitive nl.adamg.baizel.internal.common.annotations;
    requires transitive nl.adamg.baizel.core;
     requires java.logging;
    requires nl.adamg.baizel.internal.common.util;
    requires nl.adamg.baizel.internal.common.io;
    requires nl.adamg.baizel.internal.bootstrap.util;
    requires nl.adamg.baizel.internal.common.java;
    requires nl.adamg.baizel.internal.jar;
}
