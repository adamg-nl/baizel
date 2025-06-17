package nl.adamg.baizel.core.api;

import java.nio.file.Path;

/// - API:    [nl.adamg.baizel.core.api.BaizelOptions]
/// - Entity: [nl.adamg.baizel.core.entities.BaizelOptions]
/// - Impl:   [nl.adamg.baizel.core.impl.BaizelOptionsImpl]
@SuppressWarnings("JavadocReference")
public interface BaizelOptions {
    int workerCount();
    Path projectRoot();
    @Override String toString();
    @Override boolean equals(Object other);
    @Override int hashCode();
}
