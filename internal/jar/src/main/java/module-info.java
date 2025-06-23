module nl.adamg.baizel.internal.jar {
    exports nl.adamg.baizel.internal.jar;
    requires nl.adamg.baizel.internal.common.annotations;
    requires net.java.truevfs.access;
    requires net.java.truevfs.comp.zipdriver;
    requires net.java.truevfs.driver.zip;
    requires net.java.truevfs.kernel.spec;
    requires truecommons.shed;
    requires net.java.truecommons.key.def;
    requires java.logging;
    requires nl.adamg.baizel.internal.common.io;
    requires nl.adamg.baizel.core.api;
}