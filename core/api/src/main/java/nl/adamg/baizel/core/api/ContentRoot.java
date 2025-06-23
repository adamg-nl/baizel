package nl.adamg.baizel.core.api;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import javax.annotation.CheckForNull;

/// - API:    [nl.adamg.baizel.core.api.ContentRoot]
/// - Entity: [nl.adamg.baizel.core.entities.ContentRoot]
/// - Impl:   [nl.adamg.baizel.core.impl.ContentRootImpl]
@SuppressWarnings("JavadocReference")
public interface ContentRoot {
    Target target();
    @CheckForNull
    Class getClass(String qualifiedName);
    Collection<Class> getAllClasses();
    Module module();
    Path fullPath();
    List<ContentRoot> resources();
}
