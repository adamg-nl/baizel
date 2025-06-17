module nl.adamg.baizel.internal.compiler {
    exports nl.adamg.baizel.internal.compiler;

    requires nl.adamg.baizel.internal.common.annotations;
    requires java.compiler;
    requires nl.adamg.baizel.internal.bootstrap.util;
    requires jdk.compiler;
    requires java.logging;
}
