package nl.adamg.baizel.core.api;

import javax.annotation.CheckForNull;
import nl.adamg.baizel.internal.common.annotations.ServiceProvider;

@ServiceProvider.Interface
public interface TargetType {
    default String getTargetTypeId() {
        return getClass().getSimpleName().toLowerCase();
    }

    default String getPath() {
        return "src/" + getTargetTypeId() + "/java";
    }

    @CheckForNull
    default TargetType resourceTarget() {
        return null;
    }
}
