package nl.adamg.baizel.core.api;

/// Maven artifact coordinates, in format
/// ```
/// <ORGANIZATION>:<ARTIFACT>:<VERSION>
/// ```
///
/// - API:    [nl.adamg.baizel.core.api.ArtifactCoordinates]
/// - Entity: [nl.adamg.baizel.core.entities.ArtifactCoordinates]
/// - Model:  [nl.adamg.baizel.core.model.ArtifactCoordinates]
public interface ArtifactCoordinates {
    String organization();
    String artifact();
    String version();
    String moduleId();
}
