package nl.adamg.baizel.internal.bootstrap.util.collections;

import nl.adamg.baizel.internal.bootstrap.java.Primitives;
import nl.adamg.baizel.internal.bootstrap.java.Types;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ObjectTree {
    private static final ObjectTree EMPTY = new ObjectTree(null, null, null);
    /*@CheckForNull*/ private final Object value;
    /*@CheckForNull*/ private final ObjectTree parent;
    /*@CheckForNull*/ private final String name;

    //region factory
    /*@CheckForNull*/
    public static ObjectTree of(/*@CheckForNull*/ Object root) {
        return of(root, null, null);
    }

    public static ObjectTree empty() {
        return EMPTY;
    }

    /*@CheckForNull*/
    private static ObjectTree of(/*@CheckForNull*/ Object child, /*@CheckForNull*/ ObjectTree parent, /*@CheckForNull*/ String id) {
        if (child == null || ObjectTreeUtils.isEmpty(child)) {
            return null;
        }
        return new ObjectTree(child, parent, id);
    }

    protected ObjectTree(/*@CheckForNull*/ Object value, /*@CheckForNull*/ ObjectTree parent, /*@CheckForNull*/ String name) {
        this.value = value;
        this.parent = parent;
        this.name = name;
    }
    //endregion

    //region traversal
    public List<ObjectTree> items() {
        var nodes = new ArrayList<ObjectTree>();
        var values = list();
        for (int i = 0; i < values.size(); i++) {
            var item = values.get(i);
            var objectTree = of(item, this, String.valueOf(i));
            if (objectTree != null) {
                nodes.add(objectTree);
            }
        }
        return nodes;
    }

    public List<Map.Entry<String, ObjectTree>> entries() {
        var list = new ArrayList<Map.Entry<String, ObjectTree>>();
        for (var e : map().entrySet()) {
            if (e.getValue() != null) {
                var node = ObjectTree.of(e.getValue(), this, e.getKey());
                var stringObjectTreeEntry = (Map.Entry<String, ObjectTree>) new AbstractMap.SimpleEntry<>(e.getKey(), node);
                list.add(stringObjectTreeEntry);
            }
        }
        return list;
    }

    /**
     * @return items (of list) or values (of map)
     */
    public List<ObjectTree> children() {
        var nodes = new ArrayList<ObjectTree>();
        if (value instanceof List<?>) {
            nodes.addAll(items());
        } else if (value instanceof Map<?, ?>) {
            for (var entry : entries()) {
                nodes.add(entry.getValue());
            }
        }
        return nodes;
    }

    public ObjectTree get(String key) {
        if (value instanceof Map<?,?> map) {
            var item = map.get(key);
            if (ObjectTreeUtils.isEmpty(item)) {
                return empty();
            }
            return new ObjectTree(item, this, key);
        }
        if (value instanceof List<?> list) {
            if (key.matches("[0-9]+")) {
                var index = Integer.parseInt(key);
                if (index < list.size()) {
                    var item = list.get(index);
                    if (ObjectTreeUtils.isEmpty(item)) {
                        return empty();
                    }
                    return new ObjectTree(item, this, key);
                }
            }
            var matches = new ArrayList<>();
            for(var item : list) {
                if (item instanceof List<?> subList && subList.size() > 1) {
                    if (String.valueOf(subList.get(0)).equals(key)) {
                        matches.add(subList.size() == 2 ? subList.get(1) : subList.subList(1, subList.size()));
                    }
                }
            }
            if (! matches.isEmpty()) {
                return new ObjectTree(matches, this, key);
            }
        }
        return empty();
    }

    public ObjectTree getFirst() {
        var children = children();
        if (!children.isEmpty()) {
            return children.get(0);
        }
        return empty();
    }

    public ObjectTree get(int index) {
        return get(String.valueOf(index));
    }

    /**
     * @return starting with root, ending on own parent
     */
    public List<ObjectTree> getAncestors() {
        var ancestors = new ArrayList<ObjectTree>();
        var level = this.getParent();
        while(level != null) {
            ancestors.add(level);
            level = level.getParent();
        }
        Collections.reverse(ancestors);
        return ancestors;
    }

    public int depth() {
        return getAncestors().size();
    }

    public List<String> getPath() {
        var path = new ArrayList<String>();
        var level = this;
        while(level != null && level.name != null) {
            path.add(level.name);
            level = level.getParent();
        }
        Collections.reverse(path);
        return path;
    }

    public ObjectTree getRoot() {
        var level = this;
        while(level.parent != null) {
            level = level.parent;
        }
        return level;
    }

    public int getIndex() {
        if(name == null || parent == null || ! name.matches("[0-9]+")) {
            return -1;
        }
        var index = Integer.parseInt(name);
        if (index >= 0 && index < parent.size()) {
            return index;
        }
        return -1;
    }

    private int size() {
        if (value instanceof Collection<?> collection) {
            return collection.size();
        } else if (value instanceof Map<?,?> map) {
            return map.size();
        }
        if (isEmpty()) {
            return 0;
        }
        return 1;
    }

    /*@CheckForNull*/
    public String getName() {
        return name;
    }

    /*@CheckForNull*/
    public ObjectTree getParent() {
        return parent;
    }

    public ObjectTree getParentOrEmpty() {
        return parent != null ? parent : empty();
    }
    //endregion

    //region testing
    public boolean isEmpty() {
        return ObjectTreeUtils.isEmpty(value);
    }

    public boolean isPrimitive() {
        return value != null && ! isEmpty() && Primitives.unbox(value.getClass()).isPrimitive();
    }

    public boolean is(Class<?> type) {
        return type.isInstance(value);
    }

    public boolean isList() {
        return ! isEmpty() && value instanceof List<?>;
    }

    public boolean isMap() {
        return ! isEmpty() && value instanceof Map<?,?>;
    }

    public boolean isString() {
        return ! isEmpty() && value instanceof String;
    }

    public boolean isLeafValue() {
        return ! isEmpty() && ! isList() && ! isMap();
    }
    //endregion

    //region traversal + value reading at once
    /*@CheckForNull*/
    public <T> T get(String key, Class<T> type) {
        return get(key).as(type);
    }

    /**
     * @throws IllegalArgumentException if type is not primitive
     */
    public <T> T getPrimitive(String key, Class<T> type) throws IllegalArgumentException {
        if (! Primitives.isPrimitiveOrBoxed(type)) {
            throw new IllegalArgumentException("not primitive: " + type.getCanonicalName());
        }
        return Objects.requireNonNull(get(key).as(type));
    }
    //endregion

    //region value reading
    public List<String> keys() {
        if (value instanceof Map<?, ?>) {
            return map().entrySet()
                    .stream()
                    .filter(e -> e.getValue() != null)
                    .map(Map.Entry::getKey)
                    .toList();
        }
        return List.of();
    }

    public List<Object> values() {
        if (value instanceof Map<?, ?>) {
            return map().values()
                    .stream()
                    .filter(Objects::nonNull)
                    .toList();
        }
        return List.of();
    }

    public String string() {
        if (value instanceof String string) {
            return string;
        }
        if (value != null && Primitives.isPrimitiveOrBoxed(value.getClass())) {
            return String.valueOf(value);
        }
        return "";
    }

    public ObjectTree body() {
        if (value instanceof List<?> list && !list.isEmpty() && list.get(list.size()-1) instanceof List<?> body) {
            var bodyNode = ObjectTree.of(body, this, "" + (list.size() - 1));
            if (bodyNode != null) {
                return bodyNode;
            }
        }
        return empty();
    }

    public int integer() {
        if (value instanceof Integer i) {
            return i;
        }
        if (value instanceof String string) {
            try {
                return Integer.parseInt(string);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    public long longer() {
        if (value instanceof Long l) {
            return l;
        }
        if (value instanceof String string) {
            try {
                return Long.parseLong(string);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    public boolean bool() {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof String string) {
            return Boolean.parseBoolean(string);
        }
        return false;
    }

    public <T> List<T> list() {
        if (value instanceof List<?> list) {
           @SuppressWarnings("unchecked")
           var cast = (List<T>) Collections.unmodifiableList(list);
            return cast;
        }
        return List.of();
    }

    public Map<String, Object> map() {
        if (value instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked")
            var map = (Map<String, Object>)value;
            return map;
        }
        return Map.of();
    }

    /*@CheckForNull*/
    public <T> T as(Class<? extends T> type) {
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        if (type.isAssignableFrom(List.class)) {
            return type.cast(list());
        }
        if (type.isAssignableFrom(Map.class)) {
            return type.cast(map());
        }
        if (isEmpty()) {
            return Types.defaultValue(type);
        }
        return null;
    }

    /**
     * @throws IllegalArgumentException if type is not primitive (should be type literal like int.class !)
     */
    public <T> T asPrimitive(Class<T> primitiveType) throws IllegalArgumentException {
        var unboxed = Primitives.unbox(primitiveType); // just in case
        if (! unboxed.isPrimitive()) {
            throw new IllegalArgumentException("not primitive: " + primitiveType.getCanonicalName());
        }
        return Objects.requireNonNull(primitiveType.cast(as(unboxed)));

    }

    /*@CheckForNull*/
    public Object value() {
        return value;
    }
    //endregion

    //region value type
    @Override
    public int hashCode() {
        return value == null ? 0 : value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ObjectTree other && Objects.equals(this.value, other.value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
    //endregion
}
