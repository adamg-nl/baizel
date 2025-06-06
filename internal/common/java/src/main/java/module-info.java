module nl.adamg.baizel.internal.common.java {
    exports nl.adamg.baizel.internal.common.java;

    requires nl.adamg.baizel.internal.common.annotations;
    requires jsr305;
    requires java.compiler;
    requires nl.adamg.baizel.internal.bootstrap.java;
}