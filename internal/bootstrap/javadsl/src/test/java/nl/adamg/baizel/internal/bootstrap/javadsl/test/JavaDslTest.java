package nl.adamg.baizel.internal.bootstrap.javadsl.test;

import java.io.IOException;
import nl.adamg.baizel.internal.bootstrap.javadsl.JavaDsl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

@SuppressWarnings("UnnecessaryUnicodeEscape")
public class JavaDslTest {
    private final JavaDsl parser = new JavaDsl();

    @Test
    @DisplayName("Test basic statement parsing")
    void testBasicStatements() {
        assertParsesTo("a; b; ", List.of(List.of("a"), List.of("b")));
    }

    @Test
    @DisplayName("Test statement with nested block")
    void testStatementWithNestedBlock() {
        assertParsesTo("a { b; } ", List.of("a", List.of(List.of("b"))));
    }

    @Test
    @DisplayName("Test multiple blocks")
    void testMultipleBlocks() {
        assertParsesTo("{ a; } { b; } ", List.of(List.of(List.of(List.of("a"))), List.of(List.of(List.of("b")))));
    }

    @Test
    @DisplayName("Test three consecutive blocks")
    void testThreeConsecutiveBlocks() {
        assertParsesTo("{ a; } { b; } { c; } ",
                List.of(List.of(List.of(List.of("a"))), List.of(List.of(List.of("b"))), List.of(List.of(List.of("c")))));
    }

    @Test
    @DisplayName("Test statement followed by block")
    void testStatementFollowedByBlock() {
        assertParsesTo("a; { b; } ", List.of(List.of("a"), List.of(List.of(List.of("b")))));
    }

    @Test
    @DisplayName("Test block followed by statement")
    void testBlockFollowedByStatement() {
        assertParsesTo("{ a; } b; ", List.of(List.of(List.of(List.of("a"))), List.of("b")));
    }

    @Test
    @DisplayName("Test statement with block followed by statement")
    void testStatementWithBlockFollowedByStatement() {
        assertParsesTo("a { b; } c; ", List.of(List.of("a", List.of(List.of("b"))), List.of("c")));
    }

    @Test
    @DisplayName("Test statement followed by block followed by statement")
    void testStatementBlockStatement() {
        assertParsesTo("a; { b; } c; ", List.of(List.of("a"), List.of(List.of(List.of("b"))), List.of("c")));
    }

    @Test
    @DisplayName("Test statement followed by deeply nested block")
    void testStatementFollowedByDeeplyNestedBlock() {
        assertParsesTo("a; { { c; } } ", List.of(List.of("a"), List.of(List.of(List.of(List.of(List.of("c")))))));
    }

    @Test
    @DisplayName("Test block followed by deeply nested block")
    void testBlockFollowedByDeeplyNestedBlock() {
        assertParsesTo("{ a; } { { c; } } ",
                List.of(List.of(List.of(List.of("a"))), List.of(List.of(List.of(List.of(List.of("c")))))));
    }

    @Test
    @DisplayName("Test block followed by block with nested statement and block")
    void testBlockFollowedByBlockWithNestedStatementAndBlock() {
        assertParsesTo("{ a; } { b { c; } } ",
                List.of(List.of(List.of(List.of("a"))), List.of(List.of(List.of("b", List.of(List.of("c")))))));
    }

    @Test
    void assertRealObject() {
        var code = """
                            project nl.adamg.baizel {
                                repository maven https://repo1.maven.org/maven2/;
                    
                                dependencies maven {
                                    com.google.code.findbugs:jsr305:3.0.2;
                                    org.apache.httpcomponents:httpclient:4.5.14;
                                }
                    
                                dependencies maven {
                                    com.google.code.findbugs:jsr305:3.0.2;
                                    org.slf4j:slf4j-api:1.7.36;
                                }
                    
                                repository maven https://repo1.maven.org/maven3/;
                            }
                    """;
        var object =
                List.of("project", "nl.adamg.baizel", List.of(
                        List.of("repository", "maven", "https://repo1.maven.org/maven2/"),
                        List.of("dependencies", "maven", List.of(
                                List.of("com.google.code.findbugs:jsr305:3.0.2"),
                                List.of("org.apache.httpcomponents:httpclient:4.5.14")
                        )),
                        List.of("dependencies", "maven", List.of(
                                List.of("com.google.code.findbugs:jsr305:3.0.2"),
                                List.of("org.slf4j:slf4j-api:1.7.36")
                        )),
                        List.of("repository", "maven", "https://repo1.maven.org/maven3/")
                ));
        assertParsesTo(code, object);
    }

    @Test
    public void assertMinifiedObject() {
        var code = """
                            x x {
                                x x x;
                    
                                x x {
                                    x;
                                    x;
                                }
                    
                                x x {
                                    x;
                                    x;
                                }
                    
                                x x x;
                            }
                    """;
        var object = List.of("x", "x", List.of(
                List.of("x", "x", "x"),
                List.of("x", "x", List.of(
                        List.of("x"),
                        List.of("x")
                )),
                List.of("x", "x", List.of(
                        List.of("x"),
                        List.of("x")
                )),
                List.of("x", "x", "x")
        ));
        assertParsesTo(code, object);
    }

    @Test
    public void testNoPostprocessing() {
        assertParsesTo("a;", "a");
        assertParsesTo("aa bb cc;", List.of("aa", "bb", "cc"));
        assertParsesTo("a { b; } c;", List.of(List.of("a", List.of(List.of("b"))), List.of("c")));
        assertParsesTo("a { b b; c c; } d;", List.of(List.of("a", List.of(List.of("b", "b"), List.of("c", "c"))), List.of("d")));
        assertParsesTo("", List.of());
    }

    @Test
    public void testSingleValue() {
        assertParsesTo("a;", "a");
    }

    @Test
    public void testMultiValue() {
        assertParsesTo("aa bb cc;", List.of("aa", "bb", "cc"));
    }

    @Test
    public void testTopLevelMixedMapList() {
        assertParsesTo("a b; a c; e; f;", List.of(List.of("a", "b"), List.of("a", "c"), List.of("e"), List.of("f")));
    }

    @Test
    public void testMap() {
        assertParsesTo("aa { bb cc; }", List.of("aa", List.of(List.of("bb", "cc"))));
    }

    @Test
    public void testKeyedList() {
        assertParsesTo("aa { bb cc; bb dd; }", List.of("aa", List.of(List.of("bb", "cc"), List.of("bb", "dd"))));
    }

    @Test
    public void testListOfMapList() {
        assertParsesTo("a { b { c; } }", List.of("a", List.of(List.of("b", List.of(List.of("c"))))));
    }

    @Test
    public void testEmptyInput() {
        assertParsesTo("", List.of());
    }

    @Test
    public void testSimpleBlock() {
        assertParsesTo("x { y; }", List.of("x", List.of(List.of("y"))));
    }


    @Test
    public void testMultipleTopLevelEntries() {
        assertParsesTo("a b;", List.of("a", "b"));
        assertParsesTo("a; b c;", List.of(List.of("a"), List.of("b", "c")));
    }

    @Test
    public void testDeeplyNestedBlocks() {
        assertParsesTo("a b { c d { e; } f; }", List.of("a", "b", List.of(List.of("c", "d", List.of(List.of("e"))), List.of("f"))));
    }

    // New tests for mergeBlockEntries behavior:
    @Test
    public void testMergeSingleValues() {
        assertParsesTo("k v1; k v2;", List.of(List.of("k", "v1"), List.of("k", "v2")));
    }

    @Test
    public void testMergeMultipleValues() {
        assertParsesTo("k a b; k c d;", List.of(List.of("k", "a", "b"), List.of("k", "c", "d")));
    }

    @Test
    public void testMergeMixedValues() {
        assertParsesTo("k v; k a b;", List.of(List.of("k", "v"), List.of("k", "a", "b")));
    }

    /**
     * Helper method to verify that parsing DSL input produces expected object structure
     */
    private void assertParsesTo(String code, Object object) {
        code = normalizeWhitespace(code);
        var serialized = normalizeWhitespace(parser.write(object));
        var deserialized = parser.read(code);
        var reserialized = normalizeWhitespace(parser.write(deserialized));

        var expected =
                "serialized :" + code + "\n" +
                        "deserialized :" + object + "\n" +
                        "reserialized :" + code + "\n";
        var actual =
                "serialized :" + serialized + "\n" +
                        "deserialized :" + deserialized + "\n" +
                        "reserialized :" + reserialized + "\n";

        Assertions.assertEquals(expected, actual);

    }

    @Test
    public void testModuleInfo() {
        var parser = new JavaDsl();
        var object = parser.read(getClass().getResourceAsStream("module-info.txt"));
        object.toString();
    }

    @Test
    public void testProjectInfo() {
        var parser = new JavaDsl();
        var object = parser.read(getClass().getResourceAsStream("project-info.txt"));
        object.toString();
    }

    @Test
    public void testStoppingAtTerminalCharacters() throws IOException {
        Assertions.assertEquals("aaa", parser.readToken("aaa{ignored"));
        Assertions.assertEquals("aaa", parser.readToken("aaa;ignored"));
        Assertions.assertEquals("aaa", parser.readToken("aaa}ignored"));
        Assertions.assertEquals("aaa", parser.readToken("aaa ignored"));
    }

    @Test
    public void testParseSingleSequence() throws IOException {
        Assertions.assertEquals("}", parser.readToken("\\u007D"));
    }

    @Test
    public void testParseWithSurrounding() throws IOException {
        Assertions.assertEquals("b}c", parser.readToken("b\\u007Dc"));
    }

    @Test
    public void testMultipleUnicodeEscapes() throws IOException {
        // Multiple Unicode escapes in same token ("A": \u0041, "B": \u0042)
        Assertions.assertEquals("ABC", parser.readToken("\\u0041\\u0042C"));
    }

    @Test
    public void testIncompleteUnicodeEscape() throws IOException {
        // do not assert anything here - it's undefined behavior, write as compact implementation as possible while disregarding this
        // assume that in the input, incomplete unicode escapes never appear. they are all complete.
    }

    @Test
    public void testInvalidHexInUnicodeEscape() throws IOException {
        // do not assert anything here - it's undefined behavior, write as compact implementation as possible while disregarding this
        // assume that in the input, invalid hex digits never appear. they are all valid.
    }

    @Test
    public void testUnicodeEscapeAtEndOfInput() throws IOException {
        // Complete Unicode escape at the end of input
        Assertions.assertEquals("A", parser.readToken("\\u0041"));
    }

    @Test
    public void testBackslashNotPartOfUnicodeEscape() throws IOException {
        // do not assert anything here - it's undefined behavior, write as compact implementation as possible while disregarding this
        // assume that in the input, backslash not part of unicode escape never appears. it is always part of unicode escape.
    }

    @Test
    public void testUnicodeEscapeWithSpecialCharacters() throws IOException {
        // Unicode sequence for whitespace (space: \u0020)
        Assertions.assertEquals("A B", parser.readToken("A\\u0020B"));

        // Unicode sequence for brace ('{': \u007B)
        Assertions.assertEquals("A{", parser.readToken("A\\u007B"));

        // Unicode sequence for semicolon (';': \u003B)
        Assertions.assertEquals("A;", parser.readToken("A\\u003B"));
    }

    @Test
    public void testEmptyToken() throws IOException {
        // Empty input
        Assertions.assertEquals("", parser.readToken(""));
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
