package nl.adamg.baizel.internal.bootstrap.test;

import nl.adamg.baizel.internal.bootstrap.ConfigParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public class ConfigParserTest {
    @Test
    public void testSingleValue() throws ParseException {
        assertParsesTo("a", "a;");
    }

    @Test
    public void testMultiValue() throws ParseException {
        assertParsesTo(List.of("aa", "bb", "cc"), "aa bb cc;");
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

    private static void assertParsesTo(Object expected, String code) throws ParseException {
        Assertions.assertEquals(expected, new ConfigParser().read(code).getValue());
    }

    @Test
    public void testModuleInfo() throws IOException, ParseException {
        var parser = new ConfigParser();
        var object = parser.read(getClass().getResourceAsStream("module-info.txt"));
        object.toString();
    }

    @Test
    public void testProjectInfo() throws IOException, ParseException {
        var parser = new ConfigParser();
        var object = parser.read(getClass().getResourceAsStream("project-info.txt"));
        object.toString();
    }
}
