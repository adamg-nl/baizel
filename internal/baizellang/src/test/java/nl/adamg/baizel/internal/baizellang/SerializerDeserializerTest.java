package nl.adamg.baizel.internal.baizellang;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.annotation.CheckForNull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

// this class is used at framework bootstrap time and cannot use any libraries or dependencies, only core Java 21
public class SerializerDeserializerTest {
    BaizelLangReader reader = new BaizelLangReader();
    BaizelLangWriter writer = new BaizelLangWriter();

    @Test
    void test() {
        // these tests are the language specification
        // they cannot be changed! implementation below has to be adjusted until all of them pass
        // they are already made to ignore all the whitespace, only structure must match

        // implement support for this
        assertDeserializesTo("a;", List.of(List.of("a")));
    }

    // do not change this
    void assertDeserializesTo(String code, Object object) {
        var serialized = writer.write(object);
        Assertions.assertEquals(normalizeWhitespace(code), normalizeWhitespace(serialized), "code != serialized: " +  code);
        var deserialized = reader.read(code);
        var reserialized = writer.write(deserialized);
        Assertions.assertEquals(normalizeWhitespace(code), normalizeWhitespace(reserialized), "code != reserialized: " +  code);
        Assertions.assertEquals(deserialized, object, "deserialized != object: " +  code);
    }

    // do not change this
    private static String normalizeWhitespace(String input) {
        return input
                .trim()
                .replaceAll("\\s+", " ")
                .replace(";", " ; ")
                .replace("{", " { ")
                .replace("}", " } ")
                .replaceAll(" +", " ")
                .replace(" ;", ";")
                .trim();
    }
}

class BaizelLangWriter {
    public void write(@CheckForNull Object object, OutputStream output) throws IOException {
        var writer = new PrintWriter(output);
        if (object == null) {
            return;
        }
        // TODO: add writer implementation below and keep this comment
        writer.flush();
    }

    // do not change this
    public String write(Object object) {
        var buffer = new ByteArrayOutputStream();
        try {
            write(object, buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    // any new methods go below this line
}

class BaizelLangReader {
    public Object read(InputStream input) {
        // TODO: add reader implementation below and keep this comment
        // important: you are only allowed to read the input token by token, not by `readAllBytes()`
    }

    // do not change this
    public Object read(String input) {
        return read(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
    }

    // any new methods go below this line
}
