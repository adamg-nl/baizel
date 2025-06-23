package nl.adamg.baizel.core.api;

import java.nio.file.Path;
import java.util.List;

/// - API:    [nl.adamg.baizel.core.api.Class]
/// - Entity: [nl.adamg.baizel.core.entities.Class]
/// - Impl:   [nl.adamg.baizel.core.impl.ClassImpl]
@SuppressWarnings("JavadocReference")
public interface Class {
    String canonicalName();
    Path fullPath();
    ContentRoot sourceRoot();
    List<String> imports();
}
