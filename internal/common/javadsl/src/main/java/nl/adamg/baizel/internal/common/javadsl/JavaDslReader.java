package nl.adamg.baizel.internal.common.javadsl;

import nl.adamg.baizel.internal.bootstrap.javadsl.JavaDsl;
import nl.adamg.baizel.internal.bootstrap.util.collections.ObjectTree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JavaDslReader {
    public static ObjectTree read(Path javaDslFilePath) throws IOException {
        return ObjectTree.of(new JavaDsl().read(Files.newInputStream(javaDslFilePath)));
    }
}
