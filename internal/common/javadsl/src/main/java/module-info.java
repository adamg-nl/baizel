module nl.adamg.baizel.internal.common.javadsl {
    exports nl.adamg.baizel.internal.common.javadsl;

    requires java.logging;
    requires transitive nl.adamg.baizel.internal.bootstrap.javadsl;
    requires transitive nl.adamg.baizel.internal.bootstrap.util;
}
