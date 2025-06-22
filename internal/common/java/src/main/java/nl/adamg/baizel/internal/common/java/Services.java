package nl.adamg.baizel.internal.common.java;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public enum Services {
    ;
    private static final Map<String, List<?>> SERVICES = new ConcurrentHashMap<>();

    public static <T> List<T> get(Class<T> serviceInterface) {
        @SuppressWarnings("unchecked")
        var services = (List<T>)SERVICES.get(serviceInterface.getCanonicalName());
        if (services == null) {
            services = new ArrayList<>();
            for (var service : ServiceLoader.load(serviceInterface)) {
                services.add(service);
            }
            ((ArrayList<T>)services).trimToSize();
            SERVICES.put(serviceInterface.getCanonicalName(), services);
        }
        return services;
    }

    public static <T> T getFirst(Class<T> serviceInterface) {
        return get(serviceInterface).get(0);
    }
}
