package nl.adamg.baizel.internal.common.util.java.typeref;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/// Usage: `new TypeRef<>()` (always this way, it captures needed types implicitly from the call context)
public abstract class TypeRef<T> {
    protected TypeRef() {
    }

    private Type[] types() {
        return ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments();
    }

    public Class<T> t() {
        @SuppressWarnings("unchecked") var t = (Class<T>) types()[0];
        return t;
    }
}
