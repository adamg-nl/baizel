package nl.adamg.baizel.core.api;

/// - API:    [nl.adamg.baizel.core.api.SemanticVersion]
/// - Impl:   [nl.adamg.baizel.core.impl.SemanticVersionImpl]
/// - Entity: [nl.adamg.baizel.core.entities.SemanticVersion]
@SuppressWarnings("JavadocReference")
public interface SemanticVersion {
    int major();
    int minor();
    int patch();
    String prerelease();
    String build();
    @Override String toString();
    @Override boolean equals(Object other);
    @Override int hashCode();
}
