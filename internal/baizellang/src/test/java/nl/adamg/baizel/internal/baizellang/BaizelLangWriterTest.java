package nl.adamg.baizel.internal.baizellang;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class BaizelLangWriterTest {
    @Test
    public void testWriter() throws IOException {
        var expected = " { x y; z 1 2 3 ; y  { X 2; Z a b c ; } } ";
        var input = new LinkedHashMap<String, Object>();
        input.put("x", "y");
        input.put("z", List.of("1", 2, "3"));
        var nestedMap = new LinkedHashMap<String, Object>();
        nestedMap.put("X", 2);
        nestedMap.put("Z", List.of("a", "b", "c"));
        input.put("y", nestedMap);
        assertWritesTo(input, expected);
    }

    @Test
    public void testModuleInfoUniqueKeys() throws IOException {
        var expected = "module com.example  { exports com.example; provides com.example.spi.api.JmsService with com.example.spi.impl.JmsServiceSimpleImpl ; requires org.apache.maven.maven.resolver.provider; uses com.example.spi.api.JmsService; }  ";
        var input = List.of("module", "com.example", new TreeMap<>(Map.of(
                "exports", "com.example",
                "requires", "org.apache.maven.maven.resolver.provider",
                "uses", "com.example.spi.api.JmsService",
                "provides", List.of("com.example.spi.api.JmsService", "with", "com.example.spi.impl.JmsServiceSimpleImpl")
        )));
        assertWritesTo(input, expected);
    }

    @Test
    public void testModuleInfoRepeatingKeysList() throws IOException {
        var expected = """
                module com.example {
                    exports com.example ;
                    exports com.example.util ;
                    requires org.apache.maven.maven.resolver.provider ;
                    requires transitive com.example.common.util ;
                    uses com.example.spi.api.JmsService ;
                    provides com.example.spi.api.JmsService with com.example.spi.impl.JmsServiceSimpleImpl ;
                }
        """;
        var input = List.of("module", "com.example", List.of(
                List.of("exports", "com.example"),
                List.of("exports", "com.example.util"),
                List.of("requires", "org.apache.maven.maven.resolver.provider"),
                List.of("requires", "transitive", "com.example.common.util"),
                List.of("uses", "com.example.spi.api.JmsService"),
                List.of("provides", "com.example.spi.api.JmsService", "with", "com.example.spi.impl.JmsServiceSimpleImpl")
        ));
        assertWritesTo(input, expected);
    }

    @Test
    public void testProjectInfoRepeatingKeysList() throws IOException {
        var expected = """
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
        var input = List.of("project", "nl.adamg.baizel", List.of(
                List.of("repository", "maven", "https://repo1.maven.org/maven2/"),
                List.of("dependencies", "maven", List.of(
                        "com.google.code.findbugs:jsr305:3.0.2",
                        "org.apache.httpcomponents:httpclient:4.5.14"
                )),
                List.of("dependencies", "maven", List.of(
                        "com.google.code.findbugs:jsr305:3.0.2",
                        "org.slf4j:slf4j-api:1.7.36"
                )),
                List.of("repository", "maven", "https://repo1.maven.org/maven3/")
        ));
        assertWritesTo(input, expected);
    }


    @Test
    public void testProjectInfoRepeatingKeysNewFomat() throws IOException {
        var expected = """
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
        var input = List.of("project", "nl.adamg.baizel", Map.of(
                "repository", List.of(
                        List.of("maven", "https://repo1.maven.org/maven2/"),
                        List.of("maven", "https://repo1.maven.org/maven3/")
                ),
                "dependencies", List.of(
                        List.of("maven", List.of(
                                "com.google.code.findbugs:jsr305:3.0.2",
                                "org.apache.httpcomponents:httpclient:4.5.14"
                        )),
                        List.of("maven", List.of(
                                "com.google.code.findbugs:jsr305:3.0.2",
                                "org.slf4j:slf4j-api:1.7.36"
                        ))

                )
        ));
        assertWritesTo(input, expected);
    }


    @Test
    public void testProjectInfoRepeatingKeysNewFomatRecursiveMap() throws IOException {
        var expected = """
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
        // TODO: scope (map or list) gets braces on every second level
        // List.of("project" -> no
        //      Map.of("repository" -> yes
        //          Map.of("maven" -> no
        //              List.of("https: -> yes
        //      [Map.of(]"dependencies" -> yes
        //          Map.of("maven" -> no
        //              List.of("com.google -> yes
        var input = List.of("project", "nl.adamg.baizel", Map.of(
                "repository", Map.of(
                        "maven", List.of(
                                "https://repo1.maven.org/maven2/",
                                "https://repo1.maven.org/maven3/"
                        )
                ),
                "dependencies", Map.of(
                        "maven", List.of(
                                List.of(
                                        "com.google.code.findbugs:jsr305:3.0.2",
                                        "org.apache.httpcomponents:httpclient:4.5.14"
                                ),
                                List.of(
                                        "com.google.code.findbugs:jsr305:3.0.2",
                                        "org.slf4j:slf4j-api:1.7.36"
                                )

                        )
                )
        ));
        assertWritesTo(input, expected);
    }

    private static void assertWritesTo(Object input, String expected) throws IOException {
        var output = new ByteArrayOutputStream();
        var writer = new BaizelLangWriter(output);
        writer.write(input);
        var actual = output.toString(StandardCharsets.UTF_8);
        Assertions.assertEquals(normalizeWhitespace(expected), normalizeWhitespace(actual));
    }

    private static String normalizeWhitespace(String input) {
        return input
                .trim()
                .replaceAll("\\s+", " ")
                .replace(";", ";\n")
                .replace(" ;", ";")
                .replace("{", "{\n")
                .replace("}", "\n}\n");
    }
}
