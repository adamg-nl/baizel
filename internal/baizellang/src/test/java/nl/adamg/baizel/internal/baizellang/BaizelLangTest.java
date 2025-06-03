package nl.adamg.baizel.internal.baizellang;

import nl.adamg.baizel.internal.common.util.java.Primitives;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class BaizelLangTest {
    BaizelLangReader reader = new BaizelLangReader();
    BaizelLangWriter writer = new BaizelLangWriter();

    @Test
    void test() {
        assertEquals("a;", List.of(List.of("a")));
        assertEquals("a b;", List.of(List.of("a", "b")));
        assertEquals("a; b;", List.of(List.of("a"), List.of("b")));
        assertEquals("a b c;", List.of(List.of("a", "b", "c")));
        assertEquals("aa bb; cc dd;", List.of(List.of("aa", "bb"), List.of("cc", "dd")));
        assertEquals("\n aa bb ; cc dd ; e ;", List.of(List.of("aa", "bb"), List.of("cc", "dd"), List.of("e")));

        assertEquals("{ a; }", List.of(List.of(List.of(List.of("a")))));
    }

    void assertEquals(String code, Object object) {
        var serialized = writer.write(object);
        Assertions.assertEquals(normalizeWhitespace(code), normalizeWhitespace(serialized), "code != serialized: " +  code);
        var deserialized = reader.read(code);
        var reserialized = writer.write(deserialized);
        Assertions.assertEquals(normalizeWhitespace(code), normalizeWhitespace(reserialized), "code != reserialized: " +  code);
        Assertions.assertEquals(deserialized, object, "deserialized != object: " +  code);
    }

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
        if (object instanceof List) {
            boolean firstOuter = true;
            for (Object item : (List<?>) object) {
                if (item instanceof List) {
                    if (!firstOuter) writer.write(" ");
                    firstOuter = false;
                    List<?> innerList = (List<?>) item;
                    for (int i = 0; i < innerList.size(); i++) {
                        writer.write(String.valueOf(innerList.get(i)));
                        if (i < innerList.size() - 1) {
                            writer.write(" ");
                        }
                    }
                    writer.write(";");
                } else {
                    writer.write(String.valueOf(item));
                    writer.write(";");
                }
            }
        } else if (object instanceof String || Primitives.isPrimitiveOrBoxed(object.getClass())) {
            writer.write(String.valueOf(object));
            writer.write(";");
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

class BaizelLangReader {
    public Object read(InputStream input) {
        Scanner scanner = new Scanner(input, StandardCharsets.UTF_8);
        scanner.useDelimiter(";");
        List<List<String>> outerList = new ArrayList<>();
        while (scanner.hasNext()) {
            String token = scanner.next().trim();
            if (!token.isEmpty()) {
                List<String> innerList = Arrays.asList(token.split("\\s+"));
                outerList.add(innerList);
            }
        }
        return outerList;
    }


    public Object read(String input) {
        return read(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
    }
}
