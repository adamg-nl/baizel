package nl.adamg.baizel.internal.baizellang;

import nl.adamg.baizel.internal.common.util.collections.Items;
import nl.adamg.baizel.internal.common.util.collections.ObjectTree;
import nl.adamg.baizel.internal.common.util.java.Primitives;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

public class BaizelLangWriter {
    private final Writer output;

    public BaizelLangWriter(OutputStream output) {
        this.output = new PrintWriter(output);
    }

    public void write(Object object) throws IOException {
        var tree = ObjectTree.of(object);
        if (tree == null) {
            return;
        }
        write(tree);
    }

    public void write(ObjectTree tree) throws IOException {
        if (tree.isString() || tree.isPrimitive()) {
            output.write(tree.string());
            return;
        }
        if (tree.isList()) {
            var items = tree.items();
            if (isBlock(tree)) {
                output.write(" { ");
                for (var item : items) {
                    write(item);
                    if (needsSemicolon(item.value(), true)) {
                        output.write("; ");
                    } else {
                        output.write(" ");
                    }
                }
                output.write(" } ");
            } else {
                for (var item : items) {
                    write(item);
                    output.write(" ");
                }
                if (!items.isEmpty() && needsSemicolon(Items.last(items), false)) {
                    output.write("; ");
                }
            }
        }
        if (tree.isMap()) {
            output.write(" { ");
            for(var entry : tree.entries()) {
                output.write(entry.getKey());
                output.write(" ");
                write(entry.getValue());
                if (needsSemicolon(entry.getValue().value(), false)) {
                    output.write("; ");
                }
            }
            output.write("} ");
        }
        if (tree.isPrimitive()) {
            output.write(tree.string());
        }
        output.flush();
    }

    private boolean isBlock(ObjectTree tree) {
        var level = tree.depth();
        return (level+1) % 2 == 0;
    }


    public void write2(Object object) throws IOException {
        if (object instanceof String string) {
            output.write(string);
            return;
        }
        if (object instanceof List<?> list) {
            for(var item : list) {
                write(item);
                output.write(" ");
            }
            if (! list.isEmpty() && needsSemicolon(Items.last(list), false)) {
                output.write("; ");
            }
        }
        if (object instanceof Map<?,?> map) {
            output.write(" { ");
            for(var entry : map.entrySet()) {
                output.write((String) entry.getKey());
                output.write(" ");
                write(entry.getValue());
                if (needsSemicolon(entry.getValue(), false)) {
                    output.write("; ");
                }
            }
            output.write("} ");
        }
        if (Primitives.isPrimitiveOrBoxed(object.getClass())) {
            output.write(String.valueOf(object));
        }
        output.flush();
    }

    private static boolean needsSemicolon(Object item, boolean isInBlock) {
        if (item instanceof List<?> list && !list.isEmpty() && list.get(list.size() - 1) instanceof List<?>) {
            return false;
        }
        return (item instanceof List<?>) || item instanceof String || Primitives.isPrimitiveOrBoxed(item.getClass());
//        return (!isInBlock && item instanceof List<?>) || item instanceof String || Primitives.isPrimitiveOrBoxed(item.getClass());
    }
}
