package nl.adamg.baizel.core.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import javax.annotation.CheckForNull;
import nl.adamg.baizel.core.api.ObjectTree;
import nl.adamg.baizel.internal.bootstrap.javadsl.JavaDsl;
import nl.adamg.baizel.internal.common.util.collections.Items;

/// API:  [nl.adamg.baizel.core.api.ObjectTree]
/// Impl: [nl.adamg.baizel.core.impl.ObjectTreeImpl]
public class ObjectTreeImpl implements ObjectTree {
    private final nl.adamg.baizel.internal.bootstrap.util.collections.ObjectTree impl;

    //region factory
    public static ObjectTree of(@CheckForNull Object root) {
        var impl = nl.adamg.baizel.internal.bootstrap.util.collections.ObjectTree.of(root);
        return impl != null ? new ObjectTreeImpl(impl) : empty();
    }

    public static ObjectTree empty() {
        return new ObjectTreeImpl(nl.adamg.baizel.internal.bootstrap.util.collections.ObjectTree.empty());
    }

    public static ObjectTree read(Path javaDslFilePath) throws IOException {
        return of(new JavaDsl().read(Files.newInputStream(javaDslFilePath)));
    }

    public static ObjectTree read(String code) {
        var inputStream = new ByteArrayInputStream(code.getBytes(StandardCharsets.UTF_8));
        return of(new JavaDsl().read(inputStream));
    }

    private ObjectTreeImpl(nl.adamg.baizel.internal.bootstrap.util.collections.ObjectTree impl) {
        this.impl = impl;
    }
    //endregion

    //region delegate methods
    @Override
    public List<ObjectTree> items() {
        return Items.mapToList(impl.items(), ObjectTreeImpl::new);
    }

    @Override
    public List<Map.Entry<String, ObjectTree>> entries() {
        return Items.mapToList(impl.entries(), e -> new AbstractMap.SimpleEntry<>(e .getKey(), new ObjectTreeImpl(e.getValue())));
    }

    @Override
    public List<String> keys() {
        return impl.keys();
    }

    @Override
    public List<Object> values() {
        return impl.values();
    }

    @Override
    public List<ObjectTree> children() {
        return Items.mapToList(impl.children(), ObjectTreeImpl::new);
    }

    @Override
    public ObjectTree body() {
        return new ObjectTreeImpl(impl.body());
    }

    @CheckForNull
    @Override
    public Object value() {
        return impl.value();
    }

    @Override
    public ObjectTree get(String key) {
        return new ObjectTreeImpl(impl.get(key));
    }

    @Override
    public ObjectTree getFirst() {
        return new ObjectTreeImpl(impl.getFirst());
    }

    @Override
    public ObjectTree get(int index) {
        return new ObjectTreeImpl(impl.get(index));
    }

    @Override
    public List<ObjectTree> getAncestors() {
        return Items.mapToList(impl.getAncestors(), ObjectTreeImpl::new);
    }

    @Override
    public int depth() {
        return impl.depth();
    }

    @Override
    public List<String> getPath() {
        return impl.getPath();
    }

    @Override
    public ObjectTree getRoot() {
        return new ObjectTreeImpl(impl.getRoot());
    }

    @Override
    public int size() {
        return impl.size();
    }

    @CheckForNull
    @Override
    public String getName() {
        return impl.getName();
    }

    @Override
    public ObjectTree getParent() {
        return new ObjectTreeImpl(impl.getParent());
    }

    @Override
    public boolean hasParent() {
        return impl.hasParent();
    }

    @Override
    public boolean isEmpty() {
        return impl.isEmpty();
    }

    @Override
    public boolean isPrimitive() {
        return impl.isPrimitive();
    }

    @Override
    public boolean is(Class<?> type) {
        return impl.is(type);
    }

    @Override
    public boolean isList() {
        return impl.isList();
    }

    @Override
    public boolean isMap() {
        return impl.isMap();
    }

    @Override
    public boolean isString() {
        return impl.isString();
    }

    @Override
    public boolean isLeafValue() {
        return impl.isLeafValue();
    }

    @CheckForNull
    @Override
    public <T> T get(String key, Class<T> type) {
        return impl.get(key, type);
    }

    @Override
    public <T> T getLeaf(String key, Class<T> type) throws IllegalArgumentException {
        return impl.getLeaf(key, type);
    }

    @Override
    public String string() {
        return impl.string();
    }

    @Override
    public int integer() {
        return impl.integer();
    }

    @Override
    public long longer() {
        return impl.longer();
    }

    @Override
    public boolean bool() {
        return impl.bool();
    }

    @Override
    public <T> List<T> list() {
        return impl.list();
    }

    @Override
    public <T> List<T> list(Class<T> itemType) {
        return impl.list(itemType);
    }

    @Override
    public Map<String, Object> map() {
        return impl.map();
    }

    @CheckForNull
    @Override
    public <T> T as(Class<? extends T> type) {
        return impl.as(type);
    }

    @Override
    public ObjectTree unwrap() {
        return new ObjectTreeImpl(impl.unwrap());
    }

    @Override
    public <T> T asPrimitive(Class<T> primitiveType) throws IllegalArgumentException {
        return impl.asPrimitive(primitiveType);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ObjectTreeImpl other && impl.equals(other.impl);
    }

    @Override
    public int hashCode() {
        return impl.hashCode();
    }

    @Override
    public String toString() {
        return impl.toString();
    }
//endregion
}
