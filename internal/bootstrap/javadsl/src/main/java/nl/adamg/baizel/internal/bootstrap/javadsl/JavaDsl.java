package nl.adamg.baizel.internal.bootstrap.javadsl;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/// Abstract superset of 'module-info.java' syntax. Supports strings and nested lists.
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
///     "module", "example", List.of(
///         List.of("exports", "com.example"),
///         List.of("exports", "com.example.util"),
///         List.of("provides", "com.example.Service", "with", "com.example.Impl")
///     )
/// )
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
            input = wrap(input);
            var r = new PushbackReader(new InputStreamReader(input), 2); // TODO try 1
            skipWhitespace(r);
            int c;
            if ((c = r.read()) != '{') {
                r.unread(c);
            }
            var result = (Object)parseBlock(r, 0);
            result = unwrap(result);
            return result;
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
        object = wrap(object);
        var writer = new PrintWriter(output);
        writeRecursive(writer, object, 0);
        writer.flush();
    }

    //region writer implementation
    private void writeRecursive(PrintWriter w, Object o, int d) {
        if (o == null) return;
        if (!(o instanceof List<?> l)) {
            w.print(o);
            return;
        }
        if (l.isEmpty()) {
            w.print("{}");
            if (d % 2 == 1) w.print(";");
            return;
        }
        if (d % 2 == 0 && d != 0) w.print("{");
        boolean f = true;
        for (Object i : l) {
            if (!f && d % 2 == 1) w.print(" ");
            if (i instanceof List<?> && d % 2 == 1) w.print(" ");
            writeRecursive(w, i, d + 1);
            f = false;
        }
        if (d % 2 == 0 && d != 0) w.print("}");
        else if (d % 2 == 1 && !(l.isEmpty() || l.get(l.size() - 1) instanceof List<?>)) {
            w.print(";");
        }
    }

    private Object wrap(Object object) {
        if (object instanceof List<?> list) {
            if (list.stream().allMatch(i -> i instanceof List<?>)) {
                return object; // nothing to wrap
            } else {
                return List.of(object); // wrap 1 layer
            }
        } else {
            return List.of(List.of(object)); // wrap 2 layers
        }
    }
    //endregion

    //region reader implementation
    private void skipWhitespace(PushbackReader r) throws IOException {
        int c;
        while ((c = r.read()) != -1) {
            if (!Character.isWhitespace(c)) {
                r.unread(c);
                break;
            }
        }
    }

    private List<Object> parseBlock(PushbackReader r, int d) throws IOException {
        var b = new ArrayList<>();
        skipWhitespace(r);
        int c;
        while ((c = r.read()) != -1 && c != '}') {
            if (c == '{') {
                // For a block, we need to create a list containing another list (block)
                ArrayList<Object> innerList = new ArrayList<>();
                innerList.add(parseBlock(r, d + 2)); // +2 because we're going down two levels
                b.add(innerList);
            } else {
                r.unread(c);
                var s = parseStatement(r, d + 1); // +1 because statements are at odd depths
                if (s != null) b.add(s);
            }
            skipWhitespace(r);
            c = r.read();
            if (c == ';') {
                skipWhitespace(r);
            } else if (c == '}') {
                break;
            } else if (c != -1) {
                r.unread(c);
            }
        }
        return b;
    }

    private Object parseStatement(PushbackReader r, int d) throws IOException {
        var s = new ArrayList<>();
        skipWhitespace(r);
        int c;
        while ((c = r.read()) != -1) {
            if (c == '{') {
                s.add(parseBlock(r, d + 1));
                break; // End statement immediately after adding a block
            } else if (c == ';' || c == '}') {
                if (c == '}') r.unread(c);
                break;
            } else {
                r.unread(c);
                var t = parseToken(r);
                if (!t.isEmpty()) s.add(t);
            }
            skipWhitespace(r);
        }
        return s.isEmpty() ? null : s;
    }

    private String parseToken(PushbackReader r) throws IOException {
        var t = new StringBuilder();
        int c;
        while ((c = r.read()) != -1 &&
                !Character.isWhitespace(c) &&
                c != '{' && c != ';' && c != '}') {
            t.append((char)c);
        }
        if (c != -1) r.unread(c);
        return t.toString();
    }

    private Object unwrap(Object object) {
        while (object instanceof List<?> list && list.size() == 1) {
            object = list.get(0);
        }
        return object;
    }

    private InputStream wrap(InputStream input) {
        return new SequenceInputStream(Collections.enumeration(List.of(
                new ByteArrayInputStream("{ ".getBytes(StandardCharsets.UTF_8)),
                input,
                new ByteArrayInputStream(" }".getBytes(StandardCharsets.UTF_8))
        )));
    }
    //endregion
}
