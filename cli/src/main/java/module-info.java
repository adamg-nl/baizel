module nl.adamg.baizel.cli {
    exports nl.adamg.baizel.cli;

    uses nl.adamg.baizel.core.api.Task;

    provides nl.adamg.baizel.core.api.Task with
            nl.adamg.baizel.cli.tasks.Compile,
            nl.adamg.baizel.cli.tasks.Clean,
            nl.adamg.baizel.cli.tasks.Resolve;

    requires transitive nl.adamg.baizel.internal.common.annotations;
    requires transitive nl.adamg.baizel.core;
    requires java.logging;
    requires nl.adamg.baizel.internal.common.util;
    requires nl.adamg.baizel.internal.common.io;
    requires nl.adamg.baizel.internal.bootstrap.util;
    requires nl.adamg.baizel.internal.common.java;
    requires nl.adamg.baizel.internal.jar;
    requires nl.adamg.baizel.internal.common.serialization;
    requires java.compiler;
    requires jdk.compiler;
    requires nl.adamg.baizel.internal.bootstrap;
    requires nl.adamg.baizel.internal.maven;
}
