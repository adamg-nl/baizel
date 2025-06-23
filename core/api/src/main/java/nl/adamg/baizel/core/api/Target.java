package nl.adamg.baizel.core.api;

import java.util.List;
import nl.adamg.baizel.internal.common.annotations.ServiceProvider;

@ServiceProvider.Interface
public interface Target {
    default String targetId() {
        return getClass().getSimpleName().toLowerCase();
    }

    /// @return path relative to the module root
    default String contentRoot() {
        return "src/" + targetId() + "/java";
    }

    default List<Target> resources() {
        return List.of();
    }
}
