package nl.adamg.baizel.core.api;

/// Maven artifact coordinates, in format
/// ```
/// <ORGANIZATION>:<ARTIFACT>:<VERSION>
/// ```
///
/// - API:    [nl.adamg.baizel.core.api.ArtifactCoordinates]
/// - Entity: [nl.adamg.baizel.core.entities.ArtifactCoordinates]
/// - Impl:   [nl.adamg.baizel.core.impl.ArtifactCoordinatesImpl]
@SuppressWarnings("JavadocReference")
public interface ArtifactCoordinates {
    String organization();
    String artifact();
    String version();
    String moduleId();
    @Override String toString();
    @Override boolean equals(Object other);
    @Override int hashCode();
}
