module nl.adamg.baizel.cli {
    exports nl.adamg.baizel.cli;

    uses nl.adamg.baizel.cli.Command;

    provides nl.adamg.baizel.cli.Command with nl.adamg.baizel.cli.commands.Build;

    requires transitive nl.adamg.baizel.internal.common.annotations;
    requires transitive nl.adamg.baizel.core;
    requires nl.adamg.baizel.internal.common.util;
    requires java.logging;
    requires nl.adamg.baizel.internal.common.io;
    requires nl.adamg.baizel.internal.bootstrap.util;
    requires nl.adamg.baizel.internal.common.java;
}
