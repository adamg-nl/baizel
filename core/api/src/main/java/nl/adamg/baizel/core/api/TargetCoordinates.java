package nl.adamg.baizel.core.api;

/// Format: `[[@<ORG>/]<ARTIFACT>][//<PATH>][:<TARGET_NAME>]`
///
/// Example: `@com.foo/com.foo.bar//baz/qux:test`
///
/// Example: `//baz/qux`
///
/// - API:    [nl.adamg.baizel.core.api.TargetCoordinates]
/// - Entity: [nl.adamg.baizel.core.entities.TargetCoordinates]
/// - Impl:   [nl.adamg.baizel.core.impl.TargetCoordinatesImpl]
@SuppressWarnings("JavadocReference")
public interface TargetCoordinates {
    enum CoordinateKind { MODULE, FILE, ARTIFACT, INVALID }

    Target targetType();

    String organization();

    /// Equal to the qualified Java module name
    String artifact();

    String path();

    @Override
    String toString();

    @Override
    boolean equals(Object other);

    @Override
    int hashCode();
}