package nl.adamg.baizel.core.api;

/// Format: `[[@<ORG>/]<ARTIFACT>][//<PATH>][:<TARGET_NAME>]`
///
/// Example: `@com.foo/com.foo.bar//baz/qux:test`
///
/// Example: `//baz/qux`
///
/// - API:    [nl.adamg.baizel.core.api.Target]
/// - Entity: [nl.adamg.baizel.core.entities.Target]
/// - Impl:   [nl.adamg.baizel.core.impl.TargetImpl]
@SuppressWarnings("JavadocReference")
public interface Target {
    enum Type { MODULE, FILE, ARTIFACT, INVALID }
    /// @throws BaizelException if module is not found or this target is not a module nor file type target
    Module getModule(Project project);
    SourceSet sourceSet();
    String organization();
    /// Equal to the qualified Java module name
    String artifact();
    String path();
    @Override String toString();
    @Override boolean equals(Object other);
    @Override int hashCode();
}
