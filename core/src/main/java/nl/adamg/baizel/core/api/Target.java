package nl.adamg.baizel.core.api;

import nl.adamg.baizel.core.BaizelException;

/// Format: `[[@<ORG>/]<ARTIFACT>][//<PATH>][:<TARGET_NAME>]`
///
/// Example: `@com.foo/com.foo.bar//baz/qux:test`
///
/// Example: `//baz/qux`
///
/// - API:    [nl.adamg.baizel.core.api.Target]
/// - Entity: [nl.adamg.baizel.core.entities.Target]
/// - Model:  [nl.adamg.baizel.core.model.Target]
public interface Target extends Comparable<Target> {
    enum Type { MODULE, FILE, ARTIFACT, INVALID }
    /// @throws BaizelException if module is not found or this target is not a module nor file type target
    Module getModule(Project project) throws BaizelException;
    SourceSet sourceSet();
    String organization();
    /// Equal to the qualified Java module name
    String artifact();
    String path();
}
