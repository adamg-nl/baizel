package nl.adamg.baizel.core.api;

import javax.annotation.CheckForNull;
import nl.adamg.baizel.internal.common.annotations.ServiceProvider;

// TODO: consider renaming to 'SourceRootType'
@ServiceProvider.Interface
public interface SourceSet {
    default String getSourceSetId() {
        return getClass().getSimpleName().toLowerCase();
    }

    default String getPath() {
        return "src/" + getSourceSetId() + "/java";
    }

    @CheckForNull
    default SourceSet resourceSet() {
        return null;
    }
}
