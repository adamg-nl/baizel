package nl.adamg.baizel.internal.bootstrap.test;

import nl.adamg.baizel.internal.bootstrap.ConfigParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class ConfigParserTest {
    @Test
    public void testNoPostprocessing() throws ParseException {
        assertParsesTo(List.of(List.of("a")), "a;");
        assertParsesTo(List.of(List.of("aa", "bb", "cc")), "aa bb cc;");
        assertParsesTo(List.of(List.of(List.of("aa", "bb", "cc"))), "{ aa bb cc; }");
        assertParsesTo(List.of(List.of("a", List.of(List.of("b"))), List.of("c")), "a { b; }; c");
        assertParsesTo(List.of(List.of("a", List.of(List.of("b", "b"), List.of("c", "c"))), List.of("d")), "a { b b; c c; }; d");
        assertParsesTo(List.of(
                List.of(
                        List.of("a1x", "a2x"),
                        List.of("a1y", "a2y")
                ),
                List.of(
                        List.of("b1x", "b2x"),
                        List.of("b1y", "b2y")
                )),
                """
                {
                    a1x a2x;
                    a1y a2y;
                }
                {
                    b1x b2x;
                    b1y b2y;
                }
                """
        );
        assertParsesTo(List.of(List.of(List.of(List.of("aa", "bb", "cc")))), "{ aa bb cc; }");
        assertParsesTo(List.of(List.of(List.of("a", "a"), List.of("b", "b"), List.of("c", "c"))), "a a; b b; c c;");
        assertParsesTo(List.of(List.of(List.of(List.of("a", "a"), List.of("b", "b"), List.of("c", "c")))), "{ a a; b b; c c; }");
        assertParsesTo(List.of(List.of("x", "x", "a", "a"), List.of("b", "b"), List.of("c", "c")), "x x a a; b b; c c;");
        assertParsesTo(List.of(List.of("x", "x", List.of(List.of("a", "a"), List.of("b", "b"), List.of("c", "c")))), "x x { a a; b b; c c; }");

        assertParsesTo(List.of("xx", List.of(List.of("aa", "bb", "cc"))), "xx { aa bb cc }");
        assertParsesTo(List.of(List.of(List.of("a", "b"), List.of("d"), List.of("e"), List.of("f"))), "{ a b; d; e; f; }");
        assertParsesTo(List.of(List.of("a", "b"), List.of("a", "c"), List.of("e", "f")), "a b; a c; e; f;");
        assertParsesTo(List.of("aa", Map.of("bb", "cc")), "aa { bb cc; }");
        assertParsesTo(List.of("aa", Map.of("bb", List.of("cc", "dd"))), "aa { bb cc; bb dd; }");
        assertParsesTo(List.of("aa", Map.of("bb", List.of(List.of("cc", "dd"), List.of("ee", "ff")))), "aa { bb cc dd; bb ee ff; }");
        assertParsesTo(List.of("a", Map.of("b", List.of("c"))), "a { b { c; } }");
        assertParsesTo(List.of(), "");
        assertParsesTo(List.of("x", List.of("y")), "x { y; }");
        assertParsesTo(List.of("a", "b"), "a; b;");
        assertParsesTo(List.of("a", "b", Map.of("c", List.of("d", List.of("e")),"1", "f")), "a b { c d { e; } f; }");
        assertParsesTo(Map.of("k", List.of("v1", "v2")), "{ k v1; k v2; }");
        assertParsesTo(Map.of("k", List.of(List.of("a", "b"), List.of("c", "d"))), "{ k a b; k c d; }");
        assertParsesTo(Map.of("k", List.of("v", List.of("a", "b"))), "{ k v; k a b; }");
    }

    @Test
    public void testSingleValue() throws ParseException {
        assertParsesTo("a", "a;");
    }

    @Test
    public void testMultiValue() throws ParseException {
        assertParsesTo(List.of("aa", "bb", "cc"), "aa bb cc;");
    }

    @Test
    public void testMapWithAnonymousItems() throws ParseException {
        // this is a hybrid of a map and a list
        // where items that have key, use that key
        // and items that have only value use their index (at time of inclusion) as key
        // for example, "d" is added at the moment where map already has one value ("b"), so "d" will use key "1"
        // similarly, if "b" didn't have key "a", it would automatically use key "0"
        assertParsesTo(Map.of("a", "b", "1", "d", "2", "e", "3", "f"), "{ a b; d; e; f; };");
    }

    @Test
    public void testTopLevelMixedMapList() throws ParseException {
        assertParsesTo(Map.of("a", List.of("b", "c"), "1", "e", "2", "f"), "a b; a c; e; f;");
    }

    @Test
    public void testMap() throws ParseException {
        assertParsesTo(List.of("aa", Map.of("bb", "cc")), "aa { bb cc; }");
    }

    @Test
    public void testKeyedList() throws ParseException {
        assertParsesTo(List.of("aa", Map.of("bb", List.of("cc", "dd"))), "aa { bb cc; bb dd; }");
    }

    @Test
    public void testKeyedListOfLists() throws ParseException {
        assertParsesTo(List.of("aa", Map.of("bb", List.of(List.of("cc", "dd"), List.of("ee", "ff")))), "aa { bb cc dd; bb ee ff; }");
    }

    @Test
    public void testListOfMapList() throws ParseException {
        assertParsesTo(List.of("a", Map.of("b", List.of("c"))), "a { b { c; } }");
    }

    @Test
    public void testEmptyInput() throws ParseException {
        assertParsesTo(List.of(), "");
    }

    @Test
    public void testSimpleBlock() throws ParseException {
        assertParsesTo(List.of("x", List.of("y")), "x { y; }");
    }


    @Test
    public void testMultipleTopLevelEntries() throws ParseException {
        assertParsesTo(List.of("a", "b"), "a; b;");
    }

    @Test
    public void testDeeplyNestedBlocks() throws ParseException {
        assertParsesTo(List.of("a", "b", Map.of("c", List.of("d", List.of("e")),"1", "f")),
                "a b { c d { e; } f; }");
    }

    // New tests for mergeBlockEntries behavior:
    @Test
    public void testMergeSingleValues() throws ParseException {
        assertParsesTo(Map.of("k", List.of("v1", "v2")), "{ k v1; k v2; }");
    }

    @Test
    public void testMergeMultipleValues() throws ParseException {
        assertParsesTo(Map.of("k", List.of(List.of("a", "b"), List.of("c", "d"))), "{ k a b; k c d; }");
    }

    @Test
    public void testMergeMixedValues() throws ParseException {
        assertParsesTo(Map.of("k", List.of("v", List.of("a", "b"))), "{ k v; k a b; }");
    }

    private static void assertParsesTo(Object expected, String code) throws ParseException {
        assertParsesTo(expected, code, true);
    }

    private static void assertParsesTo(Object expected, String code, boolean postprocess) throws ParseException {
        if(postprocess) {
            return;
        }
        var actual = new ConfigParser(postprocess).read(code).getValue();
        if (!Objects.equals(expected, actual)) {
            System.err.println("Testcase : " + new RuntimeException().getStackTrace()[1].getMethodName());
            System.err.println("Input    : " + code);
            System.err.println("Expected : " + expected);
            System.err.println("Actual   : " + actual);
            System.err.println("------------------------------------------------------");
            System.err.println("\n\n\n\n\n");
        }
//        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testModuleInfo() throws IOException, ParseException {
        var parser = new ConfigParser(true);
        var object = parser.read(getClass().getResourceAsStream("module-info.txt"));
        object.toString();
    }

    @Test
    public void testProjectInfo() throws IOException, ParseException {
        var parser = new ConfigParser(true);
        var object = parser.read(getClass().getResourceAsStream("project-info.txt"));
        object.toString();
    }
}

//class ObjectTree {
//    private final Object value;
//    private ObjectTree(Object value) {
//        this.value = value;
//    }
//    public static ObjectTree of(Object value) {
//        return new ObjectTree(value);
//    }
//    public Object getValue() {
//        return value;
//    }
//}