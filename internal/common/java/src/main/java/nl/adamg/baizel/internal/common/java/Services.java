package nl.adamg.baizel.internal.common.java;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public final class Services {
    public static <T> List<T> get(Class<T> serviceInterface) {
        var services = new ArrayList<T>();
        for (var service : ServiceLoader.load(serviceInterface)) {
            services.add(service);
        }
        return services;
    }

    private Services(){}
}
