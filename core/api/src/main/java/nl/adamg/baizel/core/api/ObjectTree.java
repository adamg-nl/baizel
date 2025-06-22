package nl.adamg.baizel.core.api;

import java.lang.Class;
import java.util.List;
import java.util.Map;
import javax.annotation.CheckForNull;

/// API:  [nl.adamg.baizel.core.api.ObjectTree]
/// Impl: [nl.adamg.baizel.core.impl.ObjectTreeImpl]
@SuppressWarnings("JavadocReference")
public interface ObjectTree {
    List<ObjectTree> items();

    List<Map.Entry<String, ObjectTree>> entries();

    List<String> keys();

    List<Object> values();

    /**
     * @return items (of list) or values (of map)
     */
    List<ObjectTree> children();

    ObjectTree body();

    @CheckForNull
    Object value();

    ObjectTree get(String key);

    ObjectTree getFirst();

    ObjectTree get(int index);

    /**
     * @return starting with root, ending on own parent
     */
    List<ObjectTree> getAncestors();

    int depth();

    List<String> getPath();

    ObjectTree getRoot();

    int size();

    @CheckForNull
    String getName();

    /// @return empty node if there is no parent.
    /// Use [#hasParent()] to check if there is a real parent.
    ObjectTree getParent();

    boolean hasParent();

    boolean isEmpty();

    boolean isPrimitive();

    boolean is(Class<?> type);

    boolean isList();

    boolean isMap();

    boolean isString();

    boolean isLeafValue();

    @CheckForNull
    <T> T get(String key, Class<T> type);

    /**
     * @throws IllegalArgumentException if type is not primitive
     */
    <T> T getLeaf(String key, Class<T> type) throws IllegalArgumentException;

    String string();

    int integer();

    long longer();

    boolean bool();

    <T> List<T> list();

    <T> List<T> list(Class<T> itemType);

    Map<String, Object> map();

    @CheckForNull
    <T> T as(Class<? extends T> type);

    /// Unwraps all the outer layers of singleton lists, if there are any
    ObjectTree unwrap();

    /**
     * @throws IllegalArgumentException if type is not primitive (should be type literal like int.class !)
     */
    <T> T asPrimitive(Class<T> primitiveType) throws IllegalArgumentException;

    @Override
    int hashCode();

    @Override
    boolean equals(Object obj);

    @Override
    String toString();
}
