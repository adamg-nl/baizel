package nl.adamg.baizel.internal.common.javadsl;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import nl.adamg.baizel.internal.bootstrap.javadsl.JavaDsl;
import nl.adamg.baizel.internal.bootstrap.util.collections.ObjectTree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JavaDslReader {
    public static ObjectTree read(Path javaDslFilePath) throws IOException {
        return ObjectTree.of(new JavaDsl().read(Files.newInputStream(javaDslFilePath)));
    }

    public static ObjectTree read(String code) {
        var inputStream = new ByteArrayInputStream(code.getBytes(StandardCharsets.UTF_8));
        return ObjectTree.of(new JavaDsl().read(inputStream));
    }
}
