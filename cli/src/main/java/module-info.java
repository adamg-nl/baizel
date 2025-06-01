module nl.adamg.baizel.cli {
    exports nl.adamg.baizel.cli;

    requires nl.adamg.baizel.internal.common.annotations;
    requires nl.adamg.baizel.internal.common.util;
    requires transitive nl.adamg.baizel.core;
}