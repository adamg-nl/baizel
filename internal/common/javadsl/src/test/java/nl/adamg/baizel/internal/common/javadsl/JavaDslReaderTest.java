package nl.adamg.baizel.internal.common.javadsl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JavaDslReaderTest {
    @Test
    public void testRepositories() {
        var input = """
                project nl.adamg.baizel {
                    group nl.adamg;
                    repository maven https://repo1.maven.org/maven2/;
                    repository git https://github.com/adamg-nl/baizel.git;
                }
                """;
        var tree = JavaDslReader.read(input);
        var maven = tree.body().get("repository").get("maven").string();
        Assertions.assertEquals("https://repo1.maven.org/maven2/", maven);
    }
}
