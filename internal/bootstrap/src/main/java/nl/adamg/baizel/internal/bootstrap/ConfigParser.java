package nl.adamg.baizel.internal.bootstrap;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static nl.adamg.baizel.internal.bootstrap.ObjectTree.isEmpty;

/// Abstract superset of 'module-info.java' syntax. Supports strings, arrays and maps.
///
/// Doesn't assume any keywords or structures other than block and statement.
///
/// All sequences not containing ` \t\n{};` are considered plain Strings, and Strings can come in any sequences.
///
/// First String in each entry is used as a key for enclosing map.
///
/// If key repeats (in example below, `exports`), then values are merged into a list.
///
/// If entry is made of more than two identifiers, the "tail" is parsed as list of identifiers
/// (in example below, `provides`).
///
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
/// List.of("module", "com.example", Map.of(
///     "exports", List.of("com.example", "com.example.util"),
///     "provides", List.of("com.example.Service", "with", "com.example.Impl")
/// ));
/// ```
public class ConfigParser {
    private int currentOffset;
    private int currentChar;
    private InputStream input = InputStream.nullInputStream();
    private final StringBuilder debugPreview = new StringBuilder();

    public ObjectTree read(InputStream input) throws IOException, ParseException {
        var state = new ConfigParser();
        state.input = input;
        state.nextChar();
        return ObjectTree.of(state.readItem());
    }

    public ObjectTree read(String input) throws ParseException {
        try {
            return read(new ByteArrayInputStream(input.getBytes()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void nextChar() throws IOException, ParseException {
        assertSomeCurrentChar();
        if (debugPreview != null) {
            debugPreview.append((char)currentChar);
        }
        currentChar = input.read();
        currentOffset++;
    }

    private void skipWhitespace() throws IOException, ParseException {
        while (currentChar != -1 && currentChar < 33) {
            nextChar();
        }
    }

    private String readValue() throws IOException, ParseException {
        var sb = new StringBuilder();
        while (isCurrentCharNot(" \n\t{};")) {
            sb.append((char) currentChar);
            nextChar();
        }
        return sb.toString();
    }

    private boolean isCurrentCharNot(String anyOf) {
        return currentChar != -1 && anyOf.indexOf(currentChar) < 0;
    }

    private Object readItem() throws IOException, ParseException {
        assertCurrentCharIsNot(";{}");
        var subItems = new ArrayList<>();
        while (true) {
            assertCurrentCharIsNot("}");
            if (currentChar == '{') {
                var scope = readScopeWithBraces();
                if (!isEmpty(scope)) {
                    subItems.add(scope);
                }
                break;
            } else {
                var value = readValue();
                if (!value.isEmpty()) {
                    subItems.add(value);
                }
                if (currentChar == ';') {
                    nextChar();
                    break;
                }
            }
            skipWhitespace();
            assertSomeCurrentChar();
        }
        if (subItems.size() == 1) {
            return subItems.get(0);
        }
        return subItems;
    }

    private void assertCurrentCharIsNot(String anyOf) throws ParseException {
        if (! isCurrentCharNot(anyOf)) {
            throw new ParseException("unexpected " + ((char)currentChar), currentOffset);
        }
    }

    private void assertSomeCurrentChar() throws ParseException {
        if (currentChar == -1) {
            throw new ParseException("unexpected end of input", currentOffset);
        }
    }

    private Object readScopeWithBraces() throws IOException, ParseException {
        assertChar('{');
        nextChar();
        var scope = readScope();
        assertChar('}');
        nextChar();
        return scope;
    }

    private Object readScope() throws IOException, ParseException {
        var scope = (Object) new LinkedHashMap<String, Object>();
        skipWhitespace();
        while (currentChar != -1 && currentChar != '}') {
            var item = readItem();
            if (isEmpty(item)) {
                continue;
            }
            scope = ObjectTree.merge(scope, item);
            skipWhitespace();
        }

        if (scope instanceof Map<?,?> scopeMap) {
            var isNumber = (Predicate<Object>) k -> k instanceof String ks && ks.matches("[0-9]+");
            if (scopeMap.keySet().stream().allMatch(isNumber)) {
                if (scopeMap.keySet().equals(IntStream.range(0, scopeMap.size()-1))) {
                    scope = new ArrayList<>(scopeMap.values());
                }
            }
        }
        return scope;
    }

    private void assertChar(char c) throws ParseException {
        if (currentChar != c) {
            throw new ParseException("expected " + c, currentOffset);
        }
    }
}
