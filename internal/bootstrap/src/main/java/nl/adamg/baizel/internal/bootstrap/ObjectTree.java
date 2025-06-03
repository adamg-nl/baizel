package nl.adamg.baizel.internal.bootstrap;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/// `Node = String | Map<String, Node> | List<Node>`
public class ObjectTree {
    private final Object value;

    public static boolean isEmpty(Object object) {
        if (object == null) {
            return true;
        } else if (object instanceof Collection<?> collection && collection.isEmpty()) {
            return true;
        } else if (object instanceof Map<?, ?> map && map.isEmpty()) {
            return true;
        } else if (object instanceof String string && string.isEmpty()) {
            return true;
        }
        return false;
    }

    public ObjectTree merge(ObjectTree other) {
        return new ObjectTree(merge(this.value, other.value));
    }

    private ObjectTree(Object value) {
        this.value = value;
    }

    public static ObjectTree of(Object value) {
        if (value == null) {
            value = List.of();
        }
        return new ObjectTree(value);
    }

    private static ObjectTree empty() {
        return of(null);
    }

    /*@CheckForNull*/
    public <T> T getValue(int listIndex) {
        return getValue(String.valueOf(listIndex));
    }

    /*@CheckForNull*/
    public <T> T getValue(String key) {
        @SuppressWarnings("unchecked")
        var value = (T) get(key).value;
        return value;
    }

    public <T> List<T> getList(String key) {
        return get(key).asList();
    }

    public <T> List<T> asList() {
        if (!(value instanceof List<?>)) {
            if (! isEmpty(value)) {
                @SuppressWarnings("unchecked")
                var cast = (List<T>)List.of(value);
                return cast;
            }
            return List.of();
        }
        @SuppressWarnings("unchecked")
        var list = (List<T>) value;
        if (list.isEmpty()) {
            return List.of();
        }
        return list;
    }

    public <T> Map<String, T> asMap() {
        if (!(value instanceof Map<?, ?>)) {
            if (! isEmpty(value)) {
                if (value instanceof List<?> list && list.size() > 1) {
                    var key = (String)list.get(0);
                    var tail = list.subList(1, list.size());
                    @SuppressWarnings("unchecked")
                    var map = (Map<String, T>) Map.of(key, tail);
                    return map;
                }

                @SuppressWarnings("unchecked")
                var cast = (Map<String, T>)Map.of("0", value);
                return cast;
            }
            return Map.of();
        }
        @SuppressWarnings("unchecked")
        var map = (Map<String, T>) value;
        if (map.isEmpty()) {
            return Map.of();
        }
        return map;
    }

    public <T> Map<String, T> getMap(String key) {
        return get(key).asMap();
    }

    /*@CheckForNull*/
    public String asString() {
        if (!(value instanceof String string) || string.isEmpty()) {
            var flattened = flatten(value);
            if (flattened instanceof String flattenedString) {
                return flattenedString;
            }
            return null;
        }
        return string;
    }

    private Object flatten(Object value) {
        if (! (value instanceof Collection<?> collection)) {
            return value;
        }
        if (collection.isEmpty()) {
            return null;
        }
        if (collection.size() > 1) {
            return value;
        }
        return collection.iterator().next();
    }

    /*@CheckForNull*/
    public String getString(String key) {
        return get(key).asString();
    }

    /*@CheckForNull*/
    public String getString(int index) {
        return get(index).asString();
    }

    /**
     * @return never null, empty node if key not found
     */
    /*@NonNull*/
    public ObjectTree getMap() {
        if (value instanceof Map<?,?>) {
            return this;
        }
        if (value instanceof List<?> list) {
            if (! list.isEmpty() && list.get(list.size()-1) instanceof Map<?,?> map) {
                return new ObjectTree(map);
            }
        }
        return ObjectTree.empty();
    }

    /**
     * @return never null, empty node if key not found
     */
    /*@NonNull*/
    public ObjectTree get(int listIndex) {
        return get(String.valueOf(listIndex));
    }

    /**
     * @return never null, empty node if key not found
     */
    /*@NonNull*/
    public ObjectTree get(String key) {
        if (value instanceof Map<?, ?> map) {
            return ObjectTree.of(map.get(key));
        }
        if (value instanceof List<?> list) {
            try {
                return ObjectTree.of(list.get(Integer.parseInt(key)));
            } catch (NumberFormatException e) {
                return empty();
            }
        }
        return empty();
    }

    public static Object merge(Object first, Object second) {
        if (isEmpty(first)) {
            return second;
        }
        if (isEmpty(second)) {
            return first;
        }
        if (first.getClass().isArray()) {
            first = arrayToList(first);
        }
        if (second.getClass().isArray()) {
            first = arrayToList(second);
        }
        if (first instanceof Map<?,?> && second instanceof Map<?,?>) {
            @SuppressWarnings("unchecked")
            var newFirstMap = new LinkedHashMap<>((Map<String, Object>) first);
            @SuppressWarnings("unchecked")
            var secondMap = (Map<String, Object>) second;
            for(var key : secondMap.keySet()) {
                var firstValue = newFirstMap.get(key);
                var secondValue = secondMap.get(key);
                if (firstValue != null) {
                    newFirstMap.put(key, merge(firstValue, secondValue));
                } else {
                    newFirstMap.put(key, secondValue);
                }
            }
            return newFirstMap;
        }
        if (first instanceof Map<?,?> firstMap && second instanceof List<?> secondList) {
            var newMap = new LinkedHashMap<Object, Object>(firstMap);
            if (secondList.size() == 1) {
                newMap.put(String.valueOf(newMap.size()), secondList.iterator().next());
            } else {
                var key = secondList.get(0);
                if (key instanceof String stringKey) {
                    var newItem = (secondList.size() == 2) ? secondList.get(1) : secondList.subList(1, secondList.size());
                    newMap.compute(stringKey, (k, currentItem) -> (currentItem != null) ? merge(currentItem, newItem) : newItem);
                } else {
                    newMap.put(String.valueOf(newMap.size()), secondList);
                }
            }
            return newMap;
        }
        if (first instanceof List<?> && second instanceof Map<?,?>) {
            return merge(second, first);
        }
        if (first instanceof Map<?,?> firstMap) {
            var newMap = new LinkedHashMap<Object, Object>(firstMap);
            newMap.put(String.valueOf(newMap.size()), second);
            return newMap;
        }
        if (second instanceof Map<?,?>) {
            return merge(second, first);
        }
        var newList = new ArrayList<>();
        newList.add(first);
        newList.add(second);
        return newList;
    }

    private static List<Object> arrayToList(Object array) {
        var length = Array.getLength(array);
        var list = new ArrayList<>(length);
        for(var i=0; i<length; i++) {
            list.add(Array.get(array, i));
        }
        return list;
    }

    //region generated code
    public Object getValue() {
        return value;
    }
    //endregion
}
