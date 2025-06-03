package nl.adamg.baizel.internal.bootstrap;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

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
/// declaration ::= identifier* ( identifier ';' | '{' scope '}' ) ;
/// scope ::= declaration* ;
/// file ::= scope ;
/// ```
///
/// Runtime representation:
/// ```
/// file (conceptually List<Declaration>) -> List<Object>
/// declaration (conceptually List<Identifier|Block>) -> List<Object>
/// block (conceptually List<Declaration> == List<List<Identifier|Block>>) -> List<List<Object>>
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
public class ConfigParser {
    private final boolean postprocess;
    private int currentOffset;
    private int currentChar;
    private InputStream input = InputStream.nullInputStream();
    private final StringBuilder currentLine = new StringBuilder();

    public ConfigParser(boolean postprocess) {
        this.postprocess = postprocess;
    }

    public ObjectTree read(String input) throws ParseException {
        try {
            return read(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ObjectTree read(InputStream input) throws IOException, ParseException {
        this.input = input;
        this.currentOffset = -1;

        throw null; // TODO implement
    }

    // internal utils
    private void nextChar() throws IOException {
        currentChar = input.read();
        if (currentChar == -1) {
            return;
        }
        currentOffset++;
        if (currentChar == '\r') {
            nextChar();
            return;
        }
        if (currentChar == '\n') {
            currentLine.replace(0, currentLine.length(), "");
        } else {
            currentLine.append((char) currentChar);
        }
    }

    private void skipWhitespace() throws IOException {
        while (currentChar != -1 && Character.isWhitespace(currentChar)) {
            nextChar();
        }
    }

    private ParseException syntaxError() throws ParseException {
        var column = currentLine.length();
        while (currentChar != -1 && currentChar != '\r' && currentChar != '\n') {
            try {
                nextChar();
            } catch (IOException e) {
                break;
            }
            currentLine.append((char) currentChar);
        }
        var message = "Syntax error at line \"" + currentLine + "\" column " + column
                + " (character: '" + ((char) currentChar) + "')";
        throw new ParseException(message, currentOffset);
    }
    //endregion
}
