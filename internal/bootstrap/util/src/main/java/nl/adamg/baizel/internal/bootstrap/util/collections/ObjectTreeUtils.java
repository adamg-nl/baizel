package nl.adamg.baizel.internal.bootstrap.util.collections;

import nl.adamg.baizel.internal.bootstrap.java.Types;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ObjectTreeUtils {
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

    public static boolean isEmpty(/*@CheckForNull*/ Object object) {
        if (object == null) {
            return true;
        } else if (object instanceof Collection<?> collection && collection.isEmpty()) {
            return true;
        } else if (object instanceof Map<?,?> map && map.isEmpty()) {
            return true;
        } else if (object instanceof String string && string.isEmpty()) {
            return true;
        } else if (object.equals(Types.defaultValue(object.getClass()))) {
            return true;
        }
        return false;
    }
}
