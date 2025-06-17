package nl.adamg.baizel.core.api;

import nl.adamg.baizel.internal.common.annotations.ServiceProvider;

@ServiceProvider.Interface
public interface SourceSet {
    default String getSourceSetId() {
        return getClass().getSimpleName().toLowerCase();
    }

    default String getPath() {
        return "src/" + getSourceSetId() + "/java";
    }
}
