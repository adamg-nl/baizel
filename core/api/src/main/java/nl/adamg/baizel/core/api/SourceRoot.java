package nl.adamg.baizel.core.api;

import java.util.Collection;
import javax.annotation.CheckForNull;

/// - API:    [nl.adamg.baizel.core.api.SourceRoot]
/// - Entity: [nl.adamg.baizel.core.entities.SourceRoot]
/// - Impl:   [nl.adamg.baizel.core.impl.SourceRootImpl]
@SuppressWarnings("JavadocReference")
public interface SourceRoot {
    SourceSet type();
    @CheckForNull
    Class getClass(String qualifiedName);
    Collection<Class> getAllClasses();
    Module module();
}
