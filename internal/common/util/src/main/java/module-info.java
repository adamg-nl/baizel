module nl.adamg.baizel.internal.common.util {
    exports nl.adamg.baizel.internal.common.util;
    exports nl.adamg.baizel.internal.common.util.collections;
    exports nl.adamg.baizel.internal.common.util.functions;
    exports nl.adamg.baizel.internal.common.util.concurrent;
    exports nl.adamg.baizel.internal.common.util.java.typeref;

    requires transitive nl.adamg.baizel.internal.bootstrap.util;
    requires nl.adamg.baizel.internal.common.annotations;
    requires transitive java.logging;
}