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
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SerializerDeserializerTest {
    Deserializer reader = new Deserializer();
    Serializer writer = new Serializer();

    @Test
    void test() {
        // these tests are the language specification
        // they cannot be changed! implementation below has to be adjusted until all of them pass
        // they are already made to ignore all the whitespace, only structure must match

        assertDeserializesTo("a;", List.of(List.of("a")));
        assertDeserializesTo("a b;", List.of(List.of("a", "b")));
        assertDeserializesTo("a; b;", List.of(List.of("a"), List.of("b")));
        assertDeserializesTo("a b c;", List.of(List.of("a", "b", "c")));
        assertDeserializesTo("aa bb; cc dd;", List.of(List.of("aa", "bb"), List.of("cc", "dd")));
        assertDeserializesTo("\n aa bb ; cc dd ; e ;", List.of(List.of("aa", "bb"), List.of("cc", "dd"), List.of("e")));

        assertDeserializesTo("{ a; }", List.of(List.of(List.of(List.of("a")))));
    }

    void assertDeserializesTo(String code, Object object) {
        var serialized = writer.write(object);
        Assertions.assertEquals(normalizeWhitespace(code), normalizeWhitespace(serialized), "code != serialized: " + code);
        var deserialized = reader.read(code);
        var reserialized = writer.write(deserialized);
        Assertions.assertEquals(normalizeWhitespace(code), normalizeWhitespace(reserialized), "code != reserialized: " + code);
        Assertions.assertEquals(deserialized, object, "deserialized != object: " + code);
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

// the code below is for code golf and must be as short and simple as possible in terms of structure
// but on this early development stage, identifiers and formatting should be normal
// only code branches, methods and parameters should be kept to minimum
// critical requirement: must pass all the tests above.

class Serializer {
    public void write(@CheckForNull Object object, OutputStream output) throws IOException {
        var writer = new PrintWriter(output);
        writeObject(object, writer, false);
        writer.flush();
    }

    @SuppressWarnings("unchecked")
    private void writeObject(Object object, PrintWriter writer, boolean isNested) {
        if (object == null) return;
        if (object instanceof List) {
            var list = (List<?>) object;
            var isBlock = list.stream().allMatch(e -> e instanceof List);

            if (isBlock && isNested) writer.print("{");

            for (var i = 0; i < list.size(); i++) {
                var item = list.get(i);
                if (item instanceof List) {
                    var sub = (List<?>) item;
                    for (var j = 0; j < sub.size(); j++) {
                        writer.print(sub.get(j));
                        if (j < sub.size() - 1) writer.print(" ");
                    }
                    writer.print(";");
                    if (i < list.size() - 1) writer.print(" ");
                } else {
                    writer.print(item);
                    if (i < list.size() - 1) writer.print(" ");
                }
            }

            if (isBlock && isNested) writer.print("}");
        } else {
            writer.print(object);
            writer.print(";");
        }
    }

    public String write(Object object) {
        var buffer = new ByteArrayOutputStream();
        try {
            var isTopLevelBlock = object instanceof List && ((List<?>) object).stream().allMatch(e -> e instanceof List);
            writeObject(object, new PrintWriter(buffer), !isTopLevelBlock);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }
}

class Deserializer {
    public Object read(InputStream input) {
        var scanner = new Scanner(input, StandardCharsets.UTF_8);
        scanner.useDelimiter("");
        return parse(scanner);
    }

    public Object read(String input) {
        return read(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
    }

    private Object parse(Scanner scanner) {
        List<Object> topLevel = new ArrayList<>();

        while (scanner.hasNext()) {
            skipWhitespace(scanner);
            if (!scanner.hasNext()) break;
            var ch = scanner.next().charAt(0);
            if (ch == '{') {
                topLevel.add(parseBlock(scanner));
            } else {
                scanner = prepend(scanner, ch);
                topLevel.add(parseStatement(scanner));
            }
        }

        if (topLevel.size() == 1 && topLevel.get(0) instanceof List) return topLevel.get(0);
        return topLevel;
    }

    private List<String> parseStatement(Scanner scanner) {
        List<String> tokens = new ArrayList<>();
        var current = new StringBuilder();
        while (scanner.hasNext()) {
            var ch = scanner.next().charAt(0);
            if (" \t\r\n".indexOf(ch) >= 0) {
                if (!current.isEmpty()) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
            } else if (ch == ';') {
                if (!current.isEmpty()) {
                    tokens.add(current.toString());
                }
                break;
            } else {
                current.append(ch);
            }
        }
        return tokens;
    }

    private List<List<String>> parseBlock(Scanner scanner) {
        List<List<String>> block = new ArrayList<>();
        while (scanner.hasNext()) {
            skipWhitespace(scanner);
            if (!scanner.hasNext()) break;
            var ch = scanner.next().charAt(0);
            if (ch == '}') break;
            scanner = prepend(scanner, ch);
            block.add(parseStatement(scanner));
        }
        return block;
    }

    private void skipWhitespace(Scanner scanner) {
        while (scanner.hasNext()) {
            var ch = scanner.next().charAt(0);
            if (" \t\r\n".indexOf(ch) == -1) {
                prepend(scanner, ch);
                break;
            }
        }
    }

    private Scanner prepend(Scanner scanner, char ch) {
        return new Scanner(ch + scanner.useDelimiter("").nextLine());
    }
}
