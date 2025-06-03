package nl.adamg.baizel.internal.common.util.collections;

import nl.adamg.baizel.internal.common.util.java.Types;

import javax.annotation.CheckForNull;
import java.util.Collection;
import java.util.Map;

class ObjectTreeUtils {
    static boolean isEmpty(@CheckForNull Object object) {
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
