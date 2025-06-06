module nl.adamg.baizel.internal.common.util {
    exports nl.adamg.baizel.internal.common.util.collections;
    exports nl.adamg.baizel.internal.common.util.functions;
    exports nl.adamg.baizel.internal.common.util;

    requires nl.adamg.baizel.internal.bootstrap.util;
    requires nl.adamg.baizel.internal.common.annotations;
    requires nl.adamg.baizel.internal.common.java;
    requires transitive java.logging;
}