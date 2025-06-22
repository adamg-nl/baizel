package nl.adamg.baizel.internal.bootstrap.javadsl;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.PushbackReader;
import java.io.SequenceInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/// Java DSL: generic Java-like syntax overlapping with the 'module-info.java' syntax.
/// Supports nested lists and strings ("stringly typed").
///
/// Doesn't assume any keywords or structures other than brace-delimited block made of
/// statements, and semicolon-terminated statement made of identifiers and/or trailing block.
///
/// Identifiers can contain any characters other than whitespace, braces and semicolon.
///
/// Syntax:
/// ```
/// identifier ::= /[^ \t\r\n;{}]+/ ;
/// statement ::= identifier* ( identifier ';' | block ) ;
/// block ::= '{' statement+ '}' ;
/// file ::= statement* ;
/// ```
///
/// Runtime representation:
/// ```
/// file -> List<Object>
/// statement -> List<Object>
/// block -> List<List<Object>>
/// identifier -> String
/// ```
///
/// Example:
/// ```
/// module com.example {
///     exports com.example;
///     exports com.example.util;
///     provides com.example.Service with com.example.Impl;
/// }
/// ```
/// is parsed into:
/// ```
/// List.of(
///     "module", "com.example", List.of(
///         List.of("exports", "com.example"),
///         List.of("exports", "com.example.util"),
///         List.of("provides", "com.example.Service", "with", "com.example.Impl")
///     )
/// )
/// ```
/// And can be queried like this:
/// ```
/// List<String> exports = JavaDslReader.read(moduleDefPath).body().get("exports").list(String.class);
/// // exports == List.of("com.example", "com.example.util")
/// ```
/// More examples:
/// ```
///          a; b;  ===  List.of(List.of("a"), List.of("b")));
///       a { b; }  ===  List.of("a", List.of(List.of("b"))));
///  { a; } { b; }  ===  List.of(List.of(List.of(List.of("a"))), List.of(List.of(List.of("b")))));
///      a; { b; }  ===  List.of(List.of("a"), List.of(List.of(List.of("b")))));
///      { a; } b;  ===  List.of(List.of(List.of(List.of("a"))), List.of("b")));
/// ```
public class JavaDsl {
    public Object read(String input) {
        return read(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
    }

    public Object read(InputStream input) {
        try {
            var reader = new PushbackReader(new InputStreamReader(wrap(input)), 1);
            skipWhitespace(reader);
            var character = reader.read();
            if (character != '{') {
                reader.unread(character);
            }
            return unwrap(parseBlock(reader, 0));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    public void write(Object object, OutputStream output) throws IOException {
        var writer = new PrintWriter(output);
        writeRecursive(writer, wrap(object), 0);
        writer.flush();
    }

    public String readToken(String input) throws IOException {
        return parseToken(new PushbackReader(new StringReader(input)));
    }

    //region writer implementation
    private void writeRecursive(PrintWriter writer, Object element, int depth) {
        if (element == null) {
            return;
        }
        if (!(element instanceof List<?> list)) {
            writer.print(element);
            return;
        }
        if (list.isEmpty()) {
            return;
        }

        var isStatement = depth % 2 == 1; // scope alternates between 'statement' and 'block'
        if (!isStatement && depth != 0) {
            writer.print("{");
        }

        var isFirst = true;
        for (var item : list) {
            if (!isFirst || (!isStatement && depth != 0)) {
                writer.print(" ");
            }
            writeRecursive(writer, item, depth + 1);
            isFirst = false;
        }
        if (!isStatement && depth != 0) {
            writer.print(" }");
        } else if (isStatement && !(list.get(list.size() - 1) instanceof List<?>)) {
            writer.print(";");
        }
    }

    private Object wrap(Object object) {
        if (object instanceof List<?> list) {
            return list.stream().allMatch(item -> item instanceof List<?>) ? object : List.of(object);
        }
        return List.of(List.of(object));
    }
    //endregion

    //region reader implementation
    private void skipWhitespace(PushbackReader reader) throws IOException {
        int character;
        while ((character = reader.read()) != -1 && Character.isWhitespace(character)) {
            // go ahead with read(), consuming the stream
        }
        if (character != -1) {
            reader.unread(character);
        }
    }

    private List<Object> parseBlock(PushbackReader reader, int depth) throws IOException {
        skipWhitespace(reader);
        var blockContents = new ArrayList<>();
        int character;
        while ((character = reader.read()) != -1 && character != '}') {
            if (character == '{') {
                blockContents.add(List.of(parseBlock(reader, depth + 2)));
            } else {
                reader.unread(character);
                var statement = parseStatement(reader, depth + 1);
                if (statement != null) {
                    blockContents.add(statement);
                }
            }
            skipWhitespace(reader);
            character = reader.read();
            if (character == ';') {
                skipWhitespace(reader);
            } else if (character == '}') {
                break;
            } else if (character != -1) {
                reader.unread(character);
            }
        }
        return blockContents;
    }

    private Object parseStatement(PushbackReader reader, int depth) throws IOException {
        skipWhitespace(reader);
        var statementParts = new ArrayList<>();
        int character;
        while ((character = reader.read()) != -1) {
            if (character == '{') {
                statementParts.add(parseBlock(reader, depth + 1));
                break;
            }
            if (character == ';' || character == '}') {
                if (character == '}') {
                    reader.unread(character);
                }
                break;
            }

            reader.unread(character);
            var token = parseToken(reader);
            statementParts.add(token);
            skipWhitespace(reader);
        }
        return statementParts.isEmpty() ? null : statementParts;
    }

    private String parseToken(PushbackReader reader) throws IOException {
        var token = new StringBuilder();
        int c;
        while (!((c = reader.read()) == -1 || Character.isWhitespace(c) || c == '{' || c == ';' || c == '}')) {
            if (c != '\\') {
                token.append((char) c);
                continue;
            }
            var c2 = reader.read();
            if (c2 != 'u') {
                token.append((char) c).append((char) c2);
                continue;
            }
            // handle \\uXXXX as unicode escape sequence to allow using arbitrary characters in strings
            token.append((char) Integer.parseInt(new String(new char[]{
                    (char) reader.read(), (char) reader.read(),
                    (char) reader.read(), (char) reader.read()}), 16));
        }
        if (c != -1) {
            reader.unread(c);
        }
        return token.toString();
    }

    private Object unwrap(Object object) {
        while (object instanceof List<?> list && list.size() == 1) {
            object = list.get(0);
        }
        return object;
    }

    private InputStream wrap(InputStream input) {
        return new SequenceInputStream(Collections.enumeration(List.of(
                new ByteArrayInputStream("{ ".getBytes(StandardCharsets.UTF_8)), input,
                new ByteArrayInputStream(" }".getBytes(StandardCharsets.UTF_8))
        )));
    }
    //endregion
}
