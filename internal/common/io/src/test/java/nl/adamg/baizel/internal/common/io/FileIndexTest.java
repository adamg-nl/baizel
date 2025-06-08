package nl.adamg.baizel.internal.common.io;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class FileIndexTest {
    @Test
    public void testEmptyIndex() {
        var index = FileIndex.from(List.of());
        var root = index.find("");
        Assertions.assertNotNull(root);
        Assertions.assertNull(index.find("nonexistent"));
    }

    @Test
    public void testSimplePathLookup() {
        var paths = List.of("a/b/c");
        var index = FileIndex.from(paths);

        Assertions.assertNotNull(index.find(""));
        Assertions.assertNotNull(index.find("a"));
        Assertions.assertNotNull(index.find("a/b"));
        Assertions.assertNotNull(index.find("a/b/c"));

        Assertions.assertNull(index.find("b"));
        Assertions.assertNull(index.find("a/c"));
        Assertions.assertNull(index.find("a/b/c/d"));
    }

    @Test
    public void testMultiplePaths() {
        var paths = Arrays.asList("a/b/c", "a/b/d", "x/y/z");
        var index = FileIndex.from(paths);

        Assertions.assertNotNull(index.find("a"));
        Assertions.assertNotNull(index.find("a/b"));
        Assertions.assertNotNull(index.find("a/b/c"));
        Assertions.assertNotNull(index.find("a/b/d"));
        Assertions.assertNotNull(index.find("x"));
        Assertions.assertNotNull(index.find("x/y"));
        Assertions.assertNotNull(index.find("x/y/z"));

        Assertions.assertNull(index.find("a/b/e"));
        Assertions.assertNull(index.find("x/z"));
        Assertions.assertNull(index.find("y"));
    }

    @Test
    public void testEdgeCases() {
        var paths = Arrays.asList(
                "",
                "/",
                "///",
                "a//b///c",
                "a/b/c", "a/b/c"
        );

        var index = FileIndex.from(paths);

        Assertions.assertNotNull(index.find(""));
        Assertions.assertNotNull(index.find("a/b/c"));
        Assertions.assertNotNull(index.find("//a///b//c"));
    }

    @Test
    public void testPathToPath() {
        var paths = Arrays.asList("a/b/c", "x/y/z");
        var index = FileIndex.from(paths);
        var path = index.find("a/b/c");

        var nioPath = path.toPath();
        Assertions.assertEquals(Path.of("a", "b", "c"), nioPath);
    }

    @Test
    public void testPathChildren() {
        var paths = Arrays.asList(
                "a/b/c1",
                "a/b/c2",
                "a/b/c3",
                "a/d/e"
        );
        var index = FileIndex.from(paths);

        // Test children of root
        var rootChildren = index.find("").children();
        Assertions.assertEquals(1, rootChildren.size());
        Assertions.assertEquals("a", rootChildren.get(0).toString());

        // Test children of 'a'
        var aChildren = index.find("a").children();
        Assertions.assertEquals(2, aChildren.size());
        Assertions.assertTrue(aChildren.stream().anyMatch(p -> p.toString().equals("a/b")));
        Assertions.assertTrue(aChildren.stream().anyMatch(p -> p.toString().equals("a/d")));

        // Test children of 'a/b'
        var abChildren = index.find("a/b").children();
        Assertions.assertEquals(3, abChildren.size());
        Assertions.assertTrue(abChildren.stream().anyMatch(p -> p.toString().equals("a/b/c1")));
        Assertions.assertTrue(abChildren.stream().anyMatch(p -> p.toString().equals("a/b/c2")));
        Assertions.assertTrue(abChildren.stream().anyMatch(p -> p.toString().equals("a/b/c3")));

        // Test children of leaf node (should be empty list, not null)
        var leafChildren = index.find("a/b/c1").children();
        Assertions.assertNotNull(leafChildren);
        Assertions.assertTrue(leafChildren.isEmpty());
    }

    @Test
    public void testPathDescendants() {
        var paths = Arrays.asList(
                "a/b/c1",
                "a/b/c2",
                "a/d/e/f"
        );
        var index = FileIndex.from(paths);

        // Test descendants of 'a'
        var aDescendants = index.find("a").descendants();
        Assertions.assertEquals(6, aDescendants.size());
        Assertions.assertTrue(aDescendants.stream().anyMatch(p -> p.toString().equals("a/b")));
        Assertions.assertTrue(aDescendants.stream().anyMatch(p -> p.toString().equals("a/b/c1")));
        Assertions.assertTrue(aDescendants.stream().anyMatch(p -> p.toString().equals("a/b/c2")));
        Assertions.assertTrue(aDescendants.stream().anyMatch(p -> p.toString().equals("a/d")));
        Assertions.assertTrue(aDescendants.stream().anyMatch(p -> p.toString().equals("a/d/e")));
        Assertions.assertTrue(aDescendants.stream().anyMatch(p -> p.toString().equals("a/d/e/f")));

        // Test leaf node descendants (should be empty)
        var leafDescendants = index.find("a/b/c1").descendants();
        Assertions.assertTrue(leafDescendants.isEmpty());
    }

    @Test
    public void testPathParent() {
        var paths = List.of("a/b/c");
        var index = FileIndex.from(paths);

        // Test parent of 'a/b/c'
        var c = index.find("a/b/c");
        var b = c.parent();
        Assertions.assertEquals("a/b", b.toString());

        // Test parent of 'a/b'
        var a = b.parent();
        Assertions.assertEquals("a", a.toString());

        // Test parent of 'a' (should be root)
        var root = a.parent();
        Assertions.assertEquals("", root.toString());

        // Test parent of root (should be null)
        Assertions.assertNull(root.parent());
    }

    @Test
    public void testPathAncestors() {
        var paths = List.of("a/b/c/d");
        var index = FileIndex.from(paths);

        // Test ancestors of 'a/b/c/d'
        var d = index.find("a/b/c/d");
        var ancestors = d.ancestors();

        Assertions.assertEquals(4, ancestors.size());
        Assertions.assertEquals("", ancestors.get(0).toString());
        Assertions.assertEquals("a", ancestors.get(1).toString());
        Assertions.assertEquals("a/b", ancestors.get(2).toString());
        Assertions.assertEquals("a/b/c", ancestors.get(3).toString());

        // Test ancestors of root (should be empty list)
        var rootAncestors = index.find("").ancestors();
        Assertions.assertTrue(rootAncestors.isEmpty());
    }
}
