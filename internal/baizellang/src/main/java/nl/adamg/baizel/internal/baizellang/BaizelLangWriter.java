package nl.adamg.baizel.internal.baizellang;

import nl.adamg.baizel.internal.common.util.java.Primitives;

import javax.annotation.CheckForNull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class BaizelLangWriter {
    public void write(@CheckForNull Object object, OutputStream output) throws IOException {
        var writer = new PrintWriter(output);
        if (object == null) {
            return;
        }
        if (object instanceof String || Primitives.isPrimitiveOrBoxed(object.getClass())) {
            writer.write(String.valueOf(object));
        }
        writer.flush();
    }

    public String write(Object object) {
        var buffer = new ByteArrayOutputStream();
        try {
            write(object, buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }
}
