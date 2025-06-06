package nl.adamg.baizel.internal.common.java;

import java.util.Collection;

public final class Types extends nl.adamg.baizel.internal.bootstrap.java.Types {
    public static Class<?> findCommonSuperClass(Collection<?> objects) {
        if(objects.isEmpty()) {
            return Object.class;
        }
        var superclass = objects.iterator().next().getClass();
        for(var object : objects) {
            while (! superclass.isAssignableFrom(object.getClass()) && ! superclass.equals(Object.class)) {
                superclass = superclass.getSuperclass();
            }
        }
        return superclass;
    }



    private Types() {}
}
