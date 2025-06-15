package nl.adamg.baizel.internal.common.util.java.typeref;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/// Usage: `new TypeRef2<>()` (always this way, it captures needed types implicitly from the call context)
public abstract class TypeRef2<T1, T2> {
    protected TypeRef2() {
    }

    private Type[] types() {
        return ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments();
    }

    public Class<T1> t1() {
        @SuppressWarnings("unchecked") var t1 = (Class<T1>) types()[0];
        return t1;
    }

    public Class<T2> t2() {
        @SuppressWarnings("unchecked") var t2 = (Class<T2>) types()[1];
        return t2;
    }
}
