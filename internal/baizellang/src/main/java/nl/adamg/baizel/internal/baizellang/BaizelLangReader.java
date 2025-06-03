package nl.adamg.baizel.internal.baizellang;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class BaizelLangReader {
    public Object read(InputStream input) {
        throw null;
    }

    public Object read(String input) {
        return read(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
    }
}
